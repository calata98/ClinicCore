package com.cliniccore.billing.application;

import com.cliniccore.audit.application.AuditService;
import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.billing.infrastructure.persistence.PatientPackageEntity;
import com.cliniccore.billing.infrastructure.persistence.PatientPackageJpaRepository;
import com.cliniccore.billing.infrastructure.persistence.TreatmentPackageEntity;
import com.cliniccore.billing.infrastructure.persistence.TreatmentPackageJpaRepository;
import com.cliniccore.patient.infrastructure.persistence.PatientJpaRepository;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.domain.ConflictException;
import com.cliniccore.shared.domain.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PackageService {

	private final TreatmentPackageJpaRepository packages;
	private final PatientPackageJpaRepository patientPackages;
	private final PatientJpaRepository patients;
	private final AuditService auditService;

	public PackageService(TreatmentPackageJpaRepository packages, PatientPackageJpaRepository patientPackages,
			PatientJpaRepository patients, AuditService auditService) {
		this.packages = packages;
		this.patientPackages = patientPackages;
		this.patients = patients;
		this.auditService = auditService;
	}

	@Transactional
	public TreatmentPackageEntity createPackage(CurrentUser user, CreatePackage command) {
		TreatmentPackageEntity treatmentPackage = packages.save(new TreatmentPackageEntity(user.clinicId(),
				command.name(), command.totalSessions(), command.price()));
		auditService.record(user, AuditAction.CREATE_PACKAGE, "TreatmentPackage", treatmentPackage.getId(),
				"Treatment package created");
		return treatmentPackage;
	}

	@Transactional(readOnly = true)
	public List<TreatmentPackageEntity> listPackages(UUID clinicId) {
		return packages.findByClinicIdAndActiveTrueOrderByNameAsc(clinicId);
	}

	@Transactional
	public PatientPackageEntity assignPackage(CurrentUser user, UUID patientId, AssignPackage command) {
		patients.findById(patientId).filter(patient -> patient.getClinicId().equals(user.clinicId()))
				.orElseThrow(() -> new NotFoundException("Patient not found"));
		TreatmentPackageEntity treatmentPackage = packages.findById(command.packageId())
				.filter(pack -> pack.getClinicId().equals(user.clinicId()))
				.filter(TreatmentPackageEntity::isActive)
				.orElseThrow(() -> new NotFoundException("Treatment package not found"));
		PatientPackageEntity patientPackage = patientPackages.save(new PatientPackageEntity(user.clinicId(), patientId,
				treatmentPackage.getId(), treatmentPackage.getTotalSessions(), command.paidAmount(),
				command.paymentMethod()));
		auditService.record(user, AuditAction.ASSIGN_PACKAGE, "PatientPackage", patientPackage.getId(),
				"Treatment package assigned");
		return patientPackage;
	}

	@Transactional(readOnly = true)
	public List<PatientPackageEntity> listPatientPackages(UUID clinicId, UUID patientId) {
		return patientPackages.findByClinicIdAndPatientIdOrderByPurchasedAtDesc(clinicId, patientId);
	}

	@Transactional
	public PatientPackageEntity consumeSession(CurrentUser user, UUID patientPackageId) {
		PatientPackageEntity patientPackage = patientPackages.findById(patientPackageId)
				.filter(pack -> pack.getClinicId().equals(user.clinicId()))
				.orElseThrow(() -> new NotFoundException("Patient package not found"));
		if (patientPackage.getRemainingSessions() <= 0) {
			throw new ConflictException("Package has no remaining sessions");
		}
		patientPackage.consumeSession();
		auditService.record(user, AuditAction.CONSUME_PACKAGE_SESSION, "PatientPackage", patientPackageId,
				"Package session consumed");
		return patientPackage;
	}

	public record CreatePackage(String name, int totalSessions, BigDecimal price) {
	}

	public record AssignPackage(UUID packageId, BigDecimal paidAmount, String paymentMethod) {
	}
}
