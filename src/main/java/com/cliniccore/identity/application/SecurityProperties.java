package com.cliniccore.identity.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cliniccore.security")
public record SecurityProperties(String jwtSecret, long accessTokenMinutes, long refreshTokenDays) {
}
