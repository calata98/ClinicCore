package com.cliniccore.exercise.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "exercise_plan_items")
public class ExercisePlanItemEntity extends BaseEntity {

	@Column(nullable = false)
	private UUID planId;

	@Column(nullable = false)
	private UUID exerciseId;

	private Integer series;

	private Integer repetitions;

	@Column(length = 160)
	private String frequency;

	@Column(length = 1000)
	private String notes;

	protected ExercisePlanItemEntity() {
	}

	public ExercisePlanItemEntity(UUID planId, UUID exerciseId, Integer series, Integer repetitions,
			String frequency, String notes) {
		this.planId = planId;
		this.exerciseId = exerciseId;
		this.series = series;
		this.repetitions = repetitions;
		this.frequency = frequency;
		this.notes = notes;
	}

	public UUID getPlanId() {
		return planId;
	}

	public UUID getExerciseId() {
		return exerciseId;
	}

	public Integer getSeries() {
		return series;
	}

	public Integer getRepetitions() {
		return repetitions;
	}

	public String getFrequency() {
		return frequency;
	}

	public String getNotes() {
		return notes;
	}
}
