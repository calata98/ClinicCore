package com.cliniccore.clinic.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "professionals")
public class ProfessionalEntity extends BaseEntity {

	@Column(nullable = false)
	private UUID clinicId;

	private UUID userId;

	@Column(nullable = false, length = 120)
	private String firstName;

	@Column(nullable = false, length = 160)
	private String lastName;

	@Column(length = 320)
	private String email;

	@Column(length = 40)
	private String phone;

	@Column(length = 20)
	private String color;

	@Column(nullable = false)
	private boolean active = true;

	protected ProfessionalEntity() {
	}

	public ProfessionalEntity(UUID clinicId, UUID userId, String firstName, String lastName, String email, String phone, String color) {
		this.clinicId = clinicId;
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
		this.color = color;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getColor() {
		return color;
	}

	public boolean isActive() {
		return active;
	}
}
