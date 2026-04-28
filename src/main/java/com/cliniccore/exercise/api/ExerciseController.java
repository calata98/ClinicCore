package com.cliniccore.exercise.api;

import com.cliniccore.exercise.application.ExerciseService;
import com.cliniccore.exercise.application.ExerciseService.AssignExerciseItem;
import com.cliniccore.exercise.application.ExerciseService.AssignExercisePlan;
import com.cliniccore.exercise.application.ExerciseService.CreateExercise;
import com.cliniccore.exercise.application.ExerciseService.PlanWithItems;
import com.cliniccore.exercise.infrastructure.persistence.ExerciseEntity;
import com.cliniccore.exercise.infrastructure.persistence.ExercisePlanEntity;
import com.cliniccore.exercise.infrastructure.persistence.ExercisePlanItemEntity;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.application.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ExerciseController {

	private final ExerciseService exerciseService;
	private final CurrentUserProvider currentUserProvider;

	public ExerciseController(ExerciseService exerciseService, CurrentUserProvider currentUserProvider) {
		this.exerciseService = exerciseService;
		this.currentUserProvider = currentUserProvider;
	}

	@PostMapping("/exercises")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL')")
	ExerciseResponse createExercise(@Valid @RequestBody CreateExerciseRequest request) {
		CurrentUser user = currentUserProvider.require();
		return ExerciseResponse.from(exerciseService.createExercise(user, request.toCommand()));
	}

	@GetMapping("/exercises")
	List<ExerciseResponse> listExercises() {
		return exerciseService.listExercises(currentUserProvider.require().clinicId()).stream().map(ExerciseResponse::from)
				.toList();
	}

	@PostMapping("/patients/{patientId}/exercise-plans")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL')")
	ExercisePlanResponse assignPlan(@PathVariable UUID patientId, @Valid @RequestBody AssignPlanRequest request) {
		return ExercisePlanResponse.from(exerciseService.assignPlan(currentUserProvider.require(), patientId,
				request.toCommand()));
	}

	@GetMapping("/patients/{patientId}/exercise-plans")
	List<ExercisePlanResponse> patientPlans(@PathVariable UUID patientId) {
		return exerciseService.patientPlans(currentUserProvider.require().clinicId(), patientId).stream()
				.map(ExercisePlanResponse::from)
				.toList();
	}

	record CreateExerciseRequest(@NotBlank String name, String description, String videoUrl, String imageUrl) {

		CreateExercise toCommand() {
			return new CreateExercise(name, description, videoUrl, imageUrl);
		}
	}

	record AssignPlanRequest(@NotNull UUID professionalId, @NotBlank String title, String notes,
			@NotNull LocalDate startsOn, @NotEmpty List<AssignItemRequest> items) {

		AssignExercisePlan toCommand() {
			return new AssignExercisePlan(professionalId, title, notes, startsOn,
					items.stream().map(AssignItemRequest::toCommand).toList());
		}
	}

	record AssignItemRequest(@NotNull UUID exerciseId, Integer series, Integer repetitions, String frequency,
			String notes) {

		AssignExerciseItem toCommand() {
			return new AssignExerciseItem(exerciseId, series, repetitions, frequency, notes);
		}
	}

	record ExerciseResponse(UUID id, String name, String description, String videoUrl, String imageUrl) {

		static ExerciseResponse from(ExerciseEntity exercise) {
			return new ExerciseResponse(exercise.getId(), exercise.getName(), exercise.getDescription(),
					exercise.getVideoUrl(), exercise.getImageUrl());
		}
	}

	record ExercisePlanResponse(UUID id, UUID patientId, UUID professionalId, String title, String notes,
			LocalDate startsOn, List<ExercisePlanItemResponse> items) {

		static ExercisePlanResponse from(PlanWithItems planWithItems) {
			ExercisePlanEntity plan = planWithItems.plan();
			return new ExercisePlanResponse(plan.getId(), plan.getPatientId(), plan.getProfessionalId(), plan.getTitle(),
					plan.getNotes(), plan.getStartsOn(),
					planWithItems.items().stream().map(ExercisePlanItemResponse::from).toList());
		}
	}

	record ExercisePlanItemResponse(UUID id, UUID exerciseId, Integer series, Integer repetitions, String frequency,
			String notes) {

		static ExercisePlanItemResponse from(ExercisePlanItemEntity item) {
			return new ExercisePlanItemResponse(item.getId(), item.getExerciseId(), item.getSeries(),
					item.getRepetitions(), item.getFrequency(), item.getNotes());
		}
	}
}
