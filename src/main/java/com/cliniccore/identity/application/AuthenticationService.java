package com.cliniccore.identity.application;

import com.cliniccore.audit.application.AuditService;
import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.identity.infrastructure.persistence.RefreshTokenEntity;
import com.cliniccore.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.cliniccore.identity.infrastructure.persistence.UserAccountEntity;
import com.cliniccore.identity.infrastructure.persistence.UserAccountJpaRepository;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.domain.UnauthorizedException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private final UserAccountJpaRepository users;
	private final RefreshTokenJpaRepository refreshTokens;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;
	private final SecurityProperties properties;
	private final AuditService auditService;

	public AuthenticationService(UserAccountJpaRepository users, RefreshTokenJpaRepository refreshTokens,
			PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService, SecurityProperties properties,
			AuditService auditService) {
		this.users = users;
		this.refreshTokens = refreshTokens;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenService;
		this.properties = properties;
		this.auditService = auditService;
	}

	@Transactional
	public AuthTokens login(String email, String password) {
		UserAccountEntity user = users.findByEmailIgnoreCase(email)
				.filter(UserAccountEntity::isActive)
				.filter(account -> passwordEncoder.matches(password, account.getPasswordHash()))
				.orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
		user.registerLogin();
		String refreshToken = newRefreshToken();
		refreshTokens.save(new RefreshTokenEntity(user.getId(), hash(refreshToken),
				Instant.now().plusSeconds(properties.refreshTokenDays() * 24 * 60 * 60)));
		auditService.record(user.getClinicId(), user.getId(), AuditAction.LOGIN, "UserAccount", user.getId(),
				"User logged in");
		return tokens(user, refreshToken);
	}

	@Transactional
	public AuthTokens refresh(String refreshToken) {
		RefreshTokenEntity storedToken = refreshTokens.findByTokenHash(hash(refreshToken))
				.filter(RefreshTokenEntity::isUsable)
				.orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
		UserAccountEntity user = users.findById(storedToken.getUserId())
				.filter(UserAccountEntity::isActive)
				.orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
		storedToken.revoke();
		String nextRefreshToken = newRefreshToken();
		refreshTokens.save(new RefreshTokenEntity(user.getId(), hash(nextRefreshToken),
				Instant.now().plusSeconds(properties.refreshTokenDays() * 24 * 60 * 60)));
		auditService.record(user.getClinicId(), user.getId(), AuditAction.REFRESH_TOKEN, "UserAccount", user.getId(),
				"Refresh token rotated");
		return tokens(user, nextRefreshToken);
	}

	@Transactional(readOnly = true)
	public UserProfile profile(CurrentUser currentUser) {
		UserAccountEntity user = users.findById(currentUser.userId())
				.orElseThrow(() -> new UnauthorizedException("Current user does not exist"));
		return new UserProfile(user.getId().toString(), user.getClinicId() == null ? null : user.getClinicId().toString(),
				user.getEmail(), user.getFullName(), user.getRole().name());
	}

	private AuthTokens tokens(UserAccountEntity user, String refreshToken) {
		return new AuthTokens(jwtTokenService.createAccessToken(user), refreshToken, properties.accessTokenMinutes() * 60,
				"Bearer");
	}

	private String newRefreshToken() {
		byte[] bytes = new byte[48];
		SECURE_RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not hash token", exception);
		}
	}

	public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds, String tokenType) {
	}

	public record UserProfile(String id, String clinicId, String email, String fullName, String role) {
	}
}
