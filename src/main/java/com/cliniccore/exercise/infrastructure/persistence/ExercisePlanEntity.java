package com.cliniccore.exercise.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.MutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "exercise_plans")
public class ExercisePlanEntity extends MutableEntity {

	@Column(nullable = false)
	private UUID clinicId;

	@Column(nullable = false)
	private UUID patientId;

	@Column(nullable = false)
	private UUID professionalId;

	@Column(nullable = false, length = 180)
	private String title;

	@Column(length = 2000)
	private String notes;

	@Column(nullable = false)
	private LocalDate startsOn;

	@Column(nullable = false)
	private boolean active = true;

	protected ExercisePlanEntity() {
	}

	public ExercisePlanEntity(UUID clinicId, UUID patientId, UUID professionalId, String title, String notes,
			LocalDate startsOn) {
		this.clinicId = clinicId;
		this.patientId = patientId;
		this.professionalId = professionalId;
		this.title = title;
		this.notes = notes;
		this.startsOn = startsOn;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public UUID getProfessionalId() {
		return professionalId;
	}

	public String getTitle() {
		return title;
	}

	public String getNotes() {
		return notes;
	}

	public LocalDate getStartsOn() {
		return startsOn;
	}

	public boolean isActive() {
		return active;
	}
}
