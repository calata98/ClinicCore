package com.cliniccore.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;

@MappedSuperclass
public abstract class MutableEntity extends BaseEntity {

	@Column(nullable = false)
	private Instant updatedAt;

	@PrePersist
	void prePersistMutable() {
		if (updatedAt == null) {
			updatedAt = Instant.now();
		}
	}

	@PreUpdate
	void preUpdateMutable() {
		updatedAt = Instant.now();
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
