package com.cliniccore.clinic.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalJpaRepository extends JpaRepository<ProfessionalEntity, UUID> {

	List<ProfessionalEntity> findByClinicIdAndActiveTrueOrderByLastNameAscFirstNameAsc(UUID clinicId);
}
