package com.cliniccore.patient.application;

import com.cliniccore.audit.application.AuditService;
import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.patient.infrastructure.persistence.PatientEntity;
import com.cliniccore.patient.infrastructure.persistence.PatientJpaRepository;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.domain.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

	private final PatientJpaRepository patients;
	private final AuditService auditService;

	public PatientService(PatientJpaRepository patients, AuditService auditService) {
		this.patients = patients;
		this.auditService = auditService;
	}

	@Transactional
	public PatientEntity create(CurrentUser user, UpsertPatient command) {
		PatientEntity patient = patients.save(new PatientEntity(user.clinicId(), command.firstName(), command.lastName(),
				command.phone(), command.email(), command.birthDate(), command.administrativeNotes(),
				command.consentAccepted()));
		auditService.record(user, AuditAction.CREATE_PATIENT, "Patient", patient.getId(), "Patient created");
		return patient;
	}

	@Transactional
	public PatientEntity update(CurrentUser user, UUID patientId, UpsertPatient command) {
		PatientEntity patient = findForClinic(user.clinicId(), patientId);
		patient.update(command.firstName(), command.lastName(), command.phone(), command.email(), command.birthDate(),
				command.administrativeNotes(), command.consentAccepted());
		return patient;
	}

	@Transactional(readOnly = true)
	public List<PatientEntity> search(UUID clinicId, String search) {
		if (search == null || search.isBlank()) {
			return patients.findTop25ByClinicIdAndActiveTrueOrderByLastNameAscFirstNameAsc(clinicId);
		}
		return patients.searchActive(clinicId, search);
	}

	@Transactional
	public PatientEntity detail(CurrentUser user, UUID patientId) {
		PatientEntity patient = findForClinic(user.clinicId(), patientId);
		auditService.record(user, AuditAction.READ_PATIENT, "Patient", patientId, "Patient detail read");
		return patient;
	}

	@Transactional
	public void deactivate(CurrentUser user, UUID patientId) {
		PatientEntity patient = findForClinic(user.clinicId(), patientId);
		patient.deactivate();
		auditService.record(user, AuditAction.DEACTIVATE_PATIENT, "Patient", patientId, "Patient deactivated");
	}

	public PatientEntity findForClinic(UUID clinicId, UUID patientId) {
		return patients.findById(patientId)
				.filter(patient -> patient.getClinicId().equals(clinicId))
				.filter(PatientEntity::isActive)
				.orElseThrow(() -> new NotFoundException("Patient not found"));
	}

	public record UpsertPatient(String firstName, String lastName, String phone, String email, LocalDate birthDate,
			String administrativeNotes, boolean consentAccepted) {
	}
}
