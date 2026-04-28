package com.cliniccore.scheduling.application;

import com.cliniccore.audit.application.AuditService;
import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.clinic.infrastructure.persistence.ClinicServiceJpaRepository;
import com.cliniccore.clinic.infrastructure.persistence.ProfessionalJpaRepository;
import com.cliniccore.clinic.infrastructure.persistence.RoomJpaRepository;
import com.cliniccore.patient.infrastructure.persistence.PatientJpaRepository;
import com.cliniccore.scheduling.domain.AppointmentRules;
import com.cliniccore.scheduling.domain.AppointmentStatus;
import com.cliniccore.scheduling.infrastructure.persistence.AppointmentEntity;
import com.cliniccore.scheduling.infrastructure.persistence.AppointmentJpaRepository;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.domain.ConflictException;
import com.cliniccore.shared.domain.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

	private static final Set<AppointmentStatus> NON_BLOCKING_STATUSES = Set.of(AppointmentStatus.CANCELLED);
	private final AppointmentJpaRepository appointments;
	private final PatientJpaRepository patients;
	private final ProfessionalJpaRepository professionals;
	private final RoomJpaRepository rooms;
	private final ClinicServiceJpaRepository services;
	private final AuditService auditService;

	public AppointmentService(AppointmentJpaRepository appointments, PatientJpaRepository patients,
			ProfessionalJpaRepository professionals, RoomJpaRepository rooms, ClinicServiceJpaRepository services,
			AuditService auditService) {
		this.appointments = appointments;
		this.patients = patients;
		this.professionals = professionals;
		this.rooms = rooms;
		this.services = services;
		this.auditService = auditService;
	}

	@Transactional
	public AppointmentEntity create(CurrentUser user, ScheduleAppointment command) {
		AppointmentRules.requireValidRange(command.startAt(), command.endAt());
		requireReferences(user.clinicId(), command.patientId(), command.professionalId(), command.roomId(),
				command.serviceId());
		requireNoOverlap(user.clinicId(), command.professionalId(), command.roomId(), command.startAt(), command.endAt(),
				null);
		AppointmentEntity appointment = appointments.save(new AppointmentEntity(user.clinicId(), command.patientId(),
				command.professionalId(), command.roomId(), command.serviceId(), command.startAt(), command.endAt(),
				command.reason()));
		auditService.record(user, AuditAction.CREATE_APPOINTMENT, "Appointment", appointment.getId(),
				"Appointment created");
		return appointment;
	}

	@Transactional(readOnly = true)
	public List<AppointmentEntity> between(UUID clinicId, Instant from, Instant to) {
		AppointmentRules.requireValidRange(from, to);
		return appointments.findByClinicIdAndStartAtBetweenOrderByStartAtAsc(clinicId, from, to);
	}

	@Transactional
	public AppointmentEntity reschedule(CurrentUser user, UUID appointmentId, RescheduleAppointment command) {
		AppointmentRules.requireValidRange(command.startAt(), command.endAt());
		AppointmentEntity appointment = findForClinic(user.clinicId(), appointmentId);
		requireReferences(user.clinicId(), appointment.getPatientId(), command.professionalId(), command.roomId(),
				appointment.getServiceId());
		requireNoOverlap(user.clinicId(), command.professionalId(), command.roomId(), command.startAt(), command.endAt(),
				appointmentId);
		appointment.reschedule(command.professionalId(), command.roomId(), command.startAt(), command.endAt());
		auditService.record(user, AuditAction.RESCHEDULE_APPOINTMENT, "Appointment", appointmentId,
				"Appointment rescheduled");
		return appointment;
	}

	@Transactional
	public AppointmentEntity cancel(CurrentUser user, UUID appointmentId, String reason) {
		AppointmentEntity appointment = findForClinic(user.clinicId(), appointmentId);
		appointment.cancel(reason);
		auditService.record(user, AuditAction.CANCEL_APPOINTMENT, "Appointment", appointmentId, "Appointment cancelled");
		return appointment;
	}

	@Transactional
	public AppointmentEntity complete(CurrentUser user, UUID appointmentId) {
		AppointmentEntity appointment = findForClinic(user.clinicId(), appointmentId);
		appointment.complete();
		auditService.record(user, AuditAction.COMPLETE_APPOINTMENT, "Appointment", appointmentId,
				"Appointment completed");
		return appointment;
	}

	@Transactional
	public AppointmentEntity markNoShow(CurrentUser user, UUID appointmentId) {
		AppointmentEntity appointment = findForClinic(user.clinicId(), appointmentId);
		appointment.markNoShow();
		auditService.record(user, AuditAction.MARK_NO_SHOW, "Appointment", appointmentId, "Appointment marked no-show");
		return appointment;
	}

	public AppointmentEntity findForClinic(UUID clinicId, UUID appointmentId) {
		return appointments.findById(appointmentId)
				.filter(appointment -> appointment.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Appointment not found"));
	}

	private void requireReferences(UUID clinicId, UUID patientId, UUID professionalId, UUID roomId, UUID serviceId) {
		patients.findById(patientId).filter(patient -> patient.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Patient not found"));
		professionals.findById(professionalId).filter(professional -> professional.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Professional not found"));
		rooms.findById(roomId).filter(room -> room.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Room not found"));
		services.findById(serviceId).filter(service -> service.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Service not found"));
	}

	private void requireNoOverlap(UUID clinicId, UUID professionalId, UUID roomId, Instant startAt, Instant endAt,
			UUID ignoredAppointmentId) {
		if (appointments.existsProfessionalOverlap(clinicId, professionalId, startAt, endAt, ignoredAppointmentId,
				NON_BLOCKING_STATUSES)) {
			throw new ConflictException("Professional already has an overlapping appointment");
		}
		if (appointments.existsRoomOverlap(clinicId, roomId, startAt, endAt, ignoredAppointmentId,
				NON_BLOCKING_STATUSES)) {
			throw new ConflictException("Room already has an overlapping appointment");
		}
	}

	public record ScheduleAppointment(UUID patientId, UUID professionalId, UUID roomId, UUID serviceId, Instant startAt,
			Instant endAt, String reason) {
	}

	public record RescheduleAppointment(UUID professionalId, UUID roomId, Instant startAt, Instant endAt) {
	}
}
