package com.cliniccore.scheduling.infrastructure.persistence;

import com.cliniccore.scheduling.domain.AppointmentStatus;
import com.cliniccore.shared.infrastructure.persistence.MutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class AppointmentEntity extends MutableEntity {

	@Column(nullable = false)
	private UUID clinicId;

	@Column(nullable = false)
	private UUID patientId;

	@Column(nullable = false)
	private UUID professionalId;

	@Column(nullable = false)
	private UUID roomId;

	@Column(nullable = false)
	private UUID serviceId;

	@Column(nullable = false)
	private Instant startAt;

	@Column(nullable = false)
	private Instant endAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private AppointmentStatus status = AppointmentStatus.SCHEDULED;

	@Column(length = 500)
	private String reason;

	@Column(length = 500)
	private String cancellationReason;

	protected AppointmentEntity() {
	}

	public AppointmentEntity(UUID clinicId, UUID patientId, UUID professionalId, UUID roomId, UUID serviceId,
			Instant startAt, Instant endAt, String reason) {
		this.clinicId = clinicId;
		this.patientId = patientId;
		this.professionalId = professionalId;
		this.roomId = roomId;
		this.serviceId = serviceId;
		this.startAt = startAt;
		this.endAt = endAt;
		this.reason = reason;
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

	public UUID getRoomId() {
		return roomId;
	}

	public UUID getServiceId() {
		return serviceId;
	}

	public Instant getStartAt() {
		return startAt;
	}

	public Instant getEndAt() {
		return endAt;
	}

	public AppointmentStatus getStatus() {
		return status;
	}

	public String getReason() {
		return reason;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public void reschedule(UUID professionalId, UUID roomId, Instant startAt, Instant endAt) {
		this.professionalId = professionalId;
		this.roomId = roomId;
		this.startAt = startAt;
		this.endAt = endAt;
		this.status = AppointmentStatus.SCHEDULED;
		this.cancellationReason = null;
	}

	public void cancel(String cancellationReason) {
		status = AppointmentStatus.CANCELLED;
		this.cancellationReason = cancellationReason;
	}

	public void complete() {
		status = AppointmentStatus.COMPLETED;
	}

	public void markNoShow() {
		status = AppointmentStatus.NO_SHOW;
	}
}
