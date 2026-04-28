package com.cliniccore.clinic.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "clinic_services")
public class ClinicServiceEntity extends BaseEntity {

	@Column(nullable = false)
	private UUID clinicId;

	@Column(nullable = false, length = 160)
	private String name;

	@Column(nullable = false)
	private int durationMinutes;

	private BigDecimal price;

	@Column(nullable = false)
	private boolean active = true;

	protected ClinicServiceEntity() {
	}

	public ClinicServiceEntity(UUID clinicId, String name, int durationMinutes, BigDecimal price) {
		this.clinicId = clinicId;
		this.name = name;
		this.durationMinutes = durationMinutes;
		this.price = price;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public String getName() {
		return name;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public boolean isActive() {
		return active;
	}
}
