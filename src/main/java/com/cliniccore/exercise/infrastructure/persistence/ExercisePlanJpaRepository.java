package com.cliniccore.exercise.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExercisePlanJpaRepository extends JpaRepository<ExercisePlanEntity, UUID> {

	List<ExercisePlanEntity> findByClinicIdAndPatientIdAndActiveTrueOrderByStartsOnDesc(UUID clinicId, UUID patientId);
}
