package com.cliniccore.billing.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientPackageJpaRepository extends JpaRepository<PatientPackageEntity, UUID> {

	List<PatientPackageEntity> findByClinicIdAndPatientIdOrderByPurchasedAtDesc(UUID clinicId, UUID patientId);

	long countByClinicIdAndActiveTrue(UUID clinicId);
}
