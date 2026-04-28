package com.cliniccore.identity.api;

import com.cliniccore.identity.application.AuthenticationService;
import com.cliniccore.identity.application.AuthenticationService.AuthTokens;
import com.cliniccore.identity.application.AuthenticationService.UserProfile;
import com.cliniccore.shared.application.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationService authenticationService;
	private final CurrentUserProvider currentUserProvider;

	public AuthController(AuthenticationService authenticationService, CurrentUserProvider currentUserProvider) {
		this.authenticationService = authenticationService;
		this.currentUserProvider = currentUserProvider;
	}

	@PostMapping("/login")
	AuthTokens login(@Valid @RequestBody LoginRequest request) {
		return authenticationService.login(request.email(), request.password());
	}

	@PostMapping("/refresh")
	AuthTokens refresh(@Valid @RequestBody RefreshRequest request) {
		return authenticationService.refresh(request.refreshToken());
	}

	@GetMapping("/me")
	UserProfile me() {
		return authenticationService.profile(currentUserProvider.require());
	}

	record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
	}

	record RefreshRequest(@NotBlank String refreshToken) {
	}
}
