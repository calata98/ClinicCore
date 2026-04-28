package com.cliniccore.treatment.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.MutableEntity;
import com.cliniccore.treatment.domain.EpisodeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "treatment_episodes")
public class TreatmentEpisodeEntity extends MutableEntity {

	@Column(nullable = false)
	private UUID clinicId;

	@Column(nullable = false)
	private UUID patientId;

	@Column(nullable = false)
	private UUID responsibleProfessionalId;

	@Column(nullable = false, length = 180)
	private String title;

	@Column(nullable = false)
	private LocalDate startDate;

	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private EpisodeStatus status = EpisodeStatus.OPEN;

	protected TreatmentEpisodeEntity() {
	}

	public TreatmentEpisodeEntity(UUID clinicId, UUID patientId, UUID responsibleProfessionalId, String title,
			LocalDate startDate) {
		this.clinicId = clinicId;
		this.patientId = patientId;
		this.responsibleProfessionalId = responsibleProfessionalId;
		this.title = title;
		this.startDate = startDate;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public UUID getResponsibleProfessionalId() {
		return responsibleProfessionalId;
	}

	public String getTitle() {
		return title;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public EpisodeStatus getStatus() {
		return status;
	}

	public void close(LocalDate endDate) {
		this.endDate = endDate;
		status = EpisodeStatus.CLOSED;
	}
}
