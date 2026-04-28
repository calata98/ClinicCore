package com.cliniccore.identity.application;

import com.cliniccore.identity.domain.Role;
import com.cliniccore.identity.infrastructure.persistence.UserAccountEntity;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.domain.UnauthorizedException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class JwtTokenService {

	private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
	private final ObjectMapper objectMapper;
	private final SecurityProperties properties;

	public JwtTokenService(ObjectMapper objectMapper, SecurityProperties properties) {
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	public String createAccessToken(UserAccountEntity user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(properties.accessTokenMinutes() * 60);
		Map<String, Object> header = new LinkedHashMap<>();
		header.put("alg", "HS256");
		header.put("typ", "JWT");
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", user.getId().toString());
		payload.put("clinicId", user.getClinicId() == null ? null : user.getClinicId().toString());
		payload.put("email", user.getEmail());
		payload.put("role", user.getRole().name());
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", expiresAt.getEpochSecond());
		String signingInput = encodeJson(header) + "." + encodeJson(payload);
		return signingInput + "." + sign(signingInput);
	}

	public CurrentUser parseAccessToken(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new UnauthorizedException("Invalid access token");
		}
		String signingInput = parts[0] + "." + parts[1];
		if (!constantTimeEquals(sign(signingInput), parts[2])) {
			throw new UnauthorizedException("Invalid access token signature");
		}
		Map<String, Object> payload = decodeJson(parts[1]);
		long exp = ((Number) payload.get("exp")).longValue();
		if (Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
			throw new UnauthorizedException("Access token expired");
		}
		UUID userId = UUID.fromString((String) payload.get("sub"));
		Object clinicValue = payload.get("clinicId");
		UUID clinicId = clinicValue == null ? null : UUID.fromString((String) clinicValue);
		String email = (String) payload.get("email");
		Role role = Role.valueOf((String) payload.get("role"));
		return new CurrentUser(userId, clinicId, email, role);
	}

	private String encodeJson(Map<String, Object> value) {
		try {
			return ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not encode JWT", exception);
		}
	}

	private Map<String, Object> decodeJson(String encoded) {
		try {
			return objectMapper.readValue(DECODER.decode(encoded), new TypeReference<>() {
			});
		}
		catch (Exception exception) {
			throw new UnauthorizedException("Invalid access token payload");
		}
	}

	private String sign(String signingInput) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(properties.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return ENCODER.encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not sign JWT", exception);
		}
	}

	private boolean constantTimeEquals(String expected, String actual) {
		byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
		byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
		if (expectedBytes.length != actualBytes.length) {
			return false;
		}
		int result = 0;
		for (int i = 0; i < expectedBytes.length; i++) {
			result |= expectedBytes[i] ^ actualBytes[i];
		}
		return result == 0;
	}
}
