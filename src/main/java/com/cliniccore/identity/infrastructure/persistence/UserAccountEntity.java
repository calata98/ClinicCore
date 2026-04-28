package com.cliniccore.identity.infrastructure.persistence;

import com.cliniccore.identity.domain.Role;
import com.cliniccore.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_accounts")
public class UserAccountEntity extends BaseEntity {

	private UUID clinicId;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false, length = 180)
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private Role role;

	@Column(nullable = false)
	private boolean active = true;

	private Instant lastLoginAt;

	protected UserAccountEntity() {
	}

	public UserAccountEntity(UUID clinicId, String email, String passwordHash, String fullName, Role role) {
		this.clinicId = clinicId;
		this.email = email.toLowerCase();
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.role = role;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getFullName() {
		return fullName;
	}

	public Role getRole() {
		return role;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getLastLoginAt() {
		return lastLoginAt;
	}

	public void registerLogin() {
		lastLoginAt = Instant.now();
	}
}
