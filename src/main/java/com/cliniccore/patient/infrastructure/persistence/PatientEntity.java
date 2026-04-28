package com.cliniccore.patient.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.MutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class PatientEntity extends MutableEntity {

	@Column(nullable = false)
	private UUID clinicId;

	@Column(nullable = false, length = 120)
	private String firstName;

	@Column(nullable = false, length = 160)
	private String lastName;

	@Column(length = 40)
	private String phone;

	@Column(length = 320)
	private String email;

	private LocalDate birthDate;

	@Column(length = 2000)
	private String administrativeNotes;

	@Column(nullable = false)
	private boolean consentAccepted;

	@Column(nullable = false)
	private boolean active = true;

	protected PatientEntity() {
	}

	public PatientEntity(UUID clinicId, String firstName, String lastName, String phone, String email,
			LocalDate birthDate, String administrativeNotes, boolean consentAccepted) {
		this.clinicId = clinicId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.email = email;
		this.birthDate = birthDate;
		this.administrativeNotes = administrativeNotes;
		this.consentAccepted = consentAccepted;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public String getAdministrativeNotes() {
		return administrativeNotes;
	}

	public boolean isConsentAccepted() {
		return consentAccepted;
	}

	public boolean isActive() {
		return active;
	}

	public void update(String firstName, String lastName, String phone, String email, LocalDate birthDate,
			String administrativeNotes, boolean consentAccepted) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.email = email;
		this.birthDate = birthDate;
		this.administrativeNotes = administrativeNotes;
		this.consentAccepted = consentAccepted;
	}

	public void deactivate() {
		active = false;
	}
}
