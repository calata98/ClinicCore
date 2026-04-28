package com.cliniccore.exercise.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "exercises")
public class ExerciseEntity extends BaseEntity {

	@Column(nullable = false)
	private UUID clinicId;

	@Column(nullable = false, length = 180)
	private String name;

	@Column(length = 2000)
	private String description;

	@Column(length = 1000)
	private String videoUrl;

	@Column(length = 1000)
	private String imageUrl;

	@Column(nullable = false)
	private boolean active = true;

	protected ExerciseEntity() {
	}

	public ExerciseEntity(UUID clinicId, String name, String description, String videoUrl, String imageUrl) {
		this.clinicId = clinicId;
		this.name = name;
		this.description = description;
		this.videoUrl = videoUrl;
		this.imageUrl = imageUrl;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public boolean isActive() {
		return active;
	}
}
