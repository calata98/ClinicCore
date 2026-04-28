package com.cliniccore.exercise.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseEntity, UUID> {

	List<ExerciseEntity> findByClinicIdAndActiveTrueOrderByNameAsc(UUID clinicId);
}
