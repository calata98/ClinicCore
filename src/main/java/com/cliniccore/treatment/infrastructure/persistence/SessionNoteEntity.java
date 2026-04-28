package com.cliniccore.treatment.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.MutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "session_notes")
public class SessionNoteEntity extends MutableEntity {

	@Column(nullable = false)
	private UUID clinicId;

	@Column(nullable = false)
	private UUID episodeId;

	private UUID appointmentId;

	@Column(nullable = false)
	private UUID professionalId;

	@Column(nullable = false)
	private LocalDate sessionDate;

	private Integer painLevel;

	@Column(length = 240)
	private String treatedArea;

	@Column(length = 1000)
	private String techniquesApplied;

	@Column(length = 4000)
	private String observations;

	@Column(length = 1000)
	private String nextRecommendation;

	protected SessionNoteEntity() {
	}

	public SessionNoteEntity(UUID clinicId, UUID episodeId, UUID appointmentId, UUID professionalId,
			LocalDate sessionDate, Integer painLevel, String treatedArea, String techniquesApplied,
			String observations, String nextRecommendation) {
		this.clinicId = clinicId;
		this.episodeId = episodeId;
		this.appointmentId = appointmentId;
		this.professionalId = professionalId;
		this.sessionDate = sessionDate;
		this.painLevel = painLevel;
		this.treatedArea = treatedArea;
		this.techniquesApplied = techniquesApplied;
		this.observations = observations;
		this.nextRecommendation = nextRecommendation;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public UUID getEpisodeId() {
		return episodeId;
	}

	public UUID getAppointmentId() {
		return appointmentId;
	}

	public UUID getProfessionalId() {
		return professionalId;
	}

	public LocalDate getSessionDate() {
		return sessionDate;
	}

	public Integer getPainLevel() {
		return painLevel;
	}

	public String getTreatedArea() {
		return treatedArea;
	}

	public String getTechniquesApplied() {
		return techniquesApplied;
	}

	public String getObservations() {
		return observations;
	}

	public String getNextRecommendation() {
		return nextRecommendation;
	}
}
