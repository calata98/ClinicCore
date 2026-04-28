package com.cliniccore.clinic.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rooms")
public class RoomEntity extends BaseEntity {

	@Column(nullable = false)
	private UUID clinicId;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false)
	private boolean active = true;

	protected RoomEntity() {
	}

	public RoomEntity(UUID clinicId, String name) {
		this.clinicId = clinicId;
		this.name = name;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public String getName() {
		return name;
	}

	public boolean isActive() {
		return active;
	}
}
