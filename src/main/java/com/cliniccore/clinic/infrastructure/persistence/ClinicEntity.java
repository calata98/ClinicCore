package com.cliniccore.clinic.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "clinics")
public class ClinicEntity extends BaseEntity {

	@Column(nullable = false, length = 160)
	private String name;

	@Column(length = 200)
	private String legalName;

	@Column(length = 40)
	private String phone;

	@Column(length = 320)
	private String email;

	@Column(nullable = false)
	private boolean active = true;

	protected ClinicEntity() {
	}

	public ClinicEntity(String name, String legalName, String phone, String email) {
		this.name = name;
		this.legalName = legalName;
		this.phone = phone;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public String getLegalName() {
		return legalName;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public boolean isActive() {
		return active;
	}
}
