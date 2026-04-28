package com.cliniccore.identity.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends BaseEntity {

	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false, length = 128)
	private String tokenHash;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant revokedAt;

	protected RefreshTokenEntity() {
	}

	public RefreshTokenEntity(UUID userId, String tokenHash, Instant expiresAt) {
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public boolean isUsable() {
		return revokedAt == null && expiresAt.isAfter(Instant.now());
	}

	public void revoke() {
		revokedAt = Instant.now();
	}
}
