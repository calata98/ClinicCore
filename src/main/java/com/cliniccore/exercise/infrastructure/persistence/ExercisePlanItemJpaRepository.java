package com.cliniccore.exercise.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExercisePlanItemJpaRepository extends JpaRepository<ExercisePlanItemEntity, UUID> {

	List<ExercisePlanItemEntity> findByPlanIdIn(List<UUID> planIds);
}
