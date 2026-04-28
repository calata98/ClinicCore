package com.cliniccore.clinic.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicServiceJpaRepository extends JpaRepository<ClinicServiceEntity, UUID> {

	List<ClinicServiceEntity> findByClinicIdAndActiveTrueOrderByNameAsc(UUID clinicId);
}
