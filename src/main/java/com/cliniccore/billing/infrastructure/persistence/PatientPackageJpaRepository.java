package com.cliniccore.billing.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientPackageJpaRepository extends JpaRepository<PatientPackageEntity, UUID> {

	List<PatientPackageEntity> findByClinicIdAndPatientIdOrderByPurchasedAtDesc(UUID clinicId, UUID patientId);

	long countByClinicIdAndActiveTrue(UUID clinicId);

	@Query("select coalesce(sum(p.paidAmount), 0) from PatientPackageEntity p where p.clinicId = :clinicId")
	BigDecimal sumPaidAmountByClinicId(@Param("clinicId") UUID clinicId);
}
