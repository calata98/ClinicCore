package com.cliniccore.billing.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreatmentPackageJpaRepository extends JpaRepository<TreatmentPackageEntity, UUID> {

	List<TreatmentPackageEntity> findByClinicIdAndActiveTrueOrderByNameAsc(UUID clinicId);
}
