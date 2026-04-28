package com.cliniccore.clinic.application;

import com.cliniccore.audit.application.AuditService;
import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.clinic.infrastructure.persistence.ClinicEntity;
import com.cliniccore.clinic.infrastructure.persistence.ClinicJpaRepository;
import com.cliniccore.clinic.infrastructure.persistence.ClinicServiceEntity;
import com.cliniccore.clinic.infrastructure.persistence.ClinicServiceJpaRepository;
import com.cliniccore.clinic.infrastructure.persistence.ProfessionalEntity;
import com.cliniccore.clinic.infrastructure.persistence.ProfessionalJpaRepository;
import com.cliniccore.clinic.infrastructure.persistence.RoomEntity;
import com.cliniccore.clinic.infrastructure.persistence.RoomJpaRepository;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.domain.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicManagementService {

	private final ClinicJpaRepository clinics;
	private final ProfessionalJpaRepository professionals;
	private final RoomJpaRepository rooms;
	private final ClinicServiceJpaRepository services;
	private final AuditService auditService;

	public ClinicManagementService(ClinicJpaRepository clinics, ProfessionalJpaRepository professionals,
			RoomJpaRepository rooms, ClinicServiceJpaRepository services, AuditService auditService) {
		this.clinics = clinics;
		this.professionals = professionals;
		this.rooms = rooms;
		this.services = services;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public ClinicEntity currentClinic(CurrentUser user) {
		return clinics.findById(user.clinicId()).filter(ClinicEntity::isActive)
				.orElseThrow(() -> new NotFoundException("Clinic not found"));
	}

	@Transactional
	public ClinicEntity createClinic(CurrentUser user, CreateClinic command) {
		ClinicEntity clinic = clinics.save(new ClinicEntity(command.name(), command.legalName(), command.phone(),
				command.email()));
		auditService.record(user, AuditAction.CREATE_CLINIC, "Clinic", clinic.getId(), "Clinic created");
		return clinic;
	}

	@Transactional
	public ProfessionalEntity createProfessional(CurrentUser user, CreateProfessional command) {
		ProfessionalEntity professional = professionals.save(new ProfessionalEntity(user.clinicId(), command.userId(),
				command.firstName(), command.lastName(), command.email(), command.phone(), command.color()));
		auditService.record(user, AuditAction.CREATE_PROFESSIONAL, "Professional", professional.getId(),
				"Professional created");
		return professional;
	}

	@Transactional(readOnly = true)
	public List<ProfessionalEntity> listProfessionals(UUID clinicId) {
		return professionals.findByClinicIdAndActiveTrueOrderByLastNameAscFirstNameAsc(clinicId);
	}

	@Transactional
	public RoomEntity createRoom(CurrentUser user, String name) {
		RoomEntity room = rooms.save(new RoomEntity(user.clinicId(), name));
		auditService.record(user, AuditAction.CREATE_ROOM, "Room", room.getId(), "Room created");
		return room;
	}

	@Transactional(readOnly = true)
	public List<RoomEntity> listRooms(UUID clinicId) {
		return rooms.findByClinicIdAndActiveTrueOrderByNameAsc(clinicId);
	}

	@Transactional
	public ClinicServiceEntity createService(CurrentUser user, CreateService command) {
		ClinicServiceEntity service = services.save(new ClinicServiceEntity(user.clinicId(), command.name(),
				command.durationMinutes(), command.price()));
		auditService.record(user, AuditAction.CREATE_SERVICE, "ClinicService", service.getId(), "Service created");
		return service;
	}

	@Transactional(readOnly = true)
	public List<ClinicServiceEntity> listServices(UUID clinicId) {
		return services.findByClinicIdAndActiveTrueOrderByNameAsc(clinicId);
	}

	public record CreateClinic(String name, String legalName, String phone, String email) {
	}

	public record CreateProfessional(UUID userId, String firstName, String lastName, String email, String phone,
			String color) {
	}

	public record CreateService(String name, int durationMinutes, BigDecimal price) {
	}
}
