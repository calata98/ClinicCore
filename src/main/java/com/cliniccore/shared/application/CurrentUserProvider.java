package com.cliniccore.shared.application;

import com.cliniccore.shared.domain.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

	public CurrentUser require() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser user)) {
			throw new UnauthorizedException("Authentication is required");
		}
		return user;
	}
}
