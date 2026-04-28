package com.cliniccore.exercise.application;

import com.cliniccore.audit.application.AuditService;
import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.clinic.infrastructure.persistence.ProfessionalJpaRepository;
import com.cliniccore.exercise.infrastructure.persistence.ExerciseEntity;
import com.cliniccore.exercise.infrastructure.persistence.ExerciseJpaRepository;
import com.cliniccore.exercise.infrastructure.persistence.ExercisePlanEntity;
import com.cliniccore.exercise.infrastructure.persistence.ExercisePlanItemEntity;
import com.cliniccore.exercise.infrastructure.persistence.ExercisePlanItemJpaRepository;
import com.cliniccore.exercise.infrastructure.persistence.ExercisePlanJpaRepository;
import com.cliniccore.patient.infrastructure.persistence.PatientJpaRepository;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.domain.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExerciseService {

	private final ExerciseJpaRepository exercises;
	private final ExercisePlanJpaRepository plans;
	private final ExercisePlanItemJpaRepository planItems;
	private final PatientJpaRepository patients;
	private final ProfessionalJpaRepository professionals;
	private final AuditService auditService;

	public ExerciseService(ExerciseJpaRepository exercises, ExercisePlanJpaRepository plans,
			ExercisePlanItemJpaRepository planItems, PatientJpaRepository patients, ProfessionalJpaRepository professionals,
			AuditService auditService) {
		this.exercises = exercises;
		this.plans = plans;
		this.planItems = planItems;
		this.patients = patients;
		this.professionals = professionals;
		this.auditService = auditService;
	}

	@Transactional
	public ExerciseEntity createExercise(CurrentUser user, CreateExercise command) {
		ExerciseEntity exercise = exercises.save(new ExerciseEntity(user.clinicId(), command.name(),
				command.description(), command.videoUrl(), command.imageUrl()));
		auditService.record(user, AuditAction.CREATE_EXERCISE, "Exercise", exercise.getId(), "Exercise created");
		return exercise;
	}

	@Transactional(readOnly = true)
	public List<ExerciseEntity> listExercises(UUID clinicId) {
		return exercises.findByClinicIdAndActiveTrueOrderByNameAsc(clinicId);
	}

	@Transactional
	public PlanWithItems assignPlan(CurrentUser user, UUID patientId, AssignExercisePlan command) {
		requirePatient(user.clinicId(), patientId);
		requireProfessional(user.clinicId(), command.professionalId());
		command.items().forEach(item -> requireExercise(user.clinicId(), item.exerciseId()));
		ExercisePlanEntity plan = plans.save(new ExercisePlanEntity(user.clinicId(), patientId,
				command.professionalId(), command.title(), command.notes(), command.startsOn()));
		List<ExercisePlanItemEntity> items = command.items().stream()
				.map(item -> new ExercisePlanItemEntity(plan.getId(), item.exerciseId(), item.series(),
						item.repetitions(), item.frequency(), item.notes()))
				.map(planItems::save)
				.toList();
		auditService.record(user, AuditAction.ASSIGN_EXERCISE_PLAN, "ExercisePlan", plan.getId(),
				"Exercise plan assigned");
		return new PlanWithItems(plan, items);
	}

	@Transactional(readOnly = true)
	public List<PlanWithItems> patientPlans(UUID clinicId, UUID patientId) {
		requirePatient(clinicId, patientId);
		List<ExercisePlanEntity> patientPlans = plans.findByClinicIdAndPatientIdAndActiveTrueOrderByStartsOnDesc(clinicId,
				patientId);
		if (patientPlans.isEmpty()) {
			return List.of();
		}
		Map<UUID, List<ExercisePlanItemEntity>> itemsByPlanId = planItems
				.findByPlanIdIn(patientPlans.stream().map(ExercisePlanEntity::getId).toList()).stream()
				.collect(Collectors.groupingBy(ExercisePlanItemEntity::getPlanId));
		return patientPlans.stream()
				.map(plan -> new PlanWithItems(plan, itemsByPlanId.getOrDefault(plan.getId(), List.of())))
				.toList();
	}

	private void requirePatient(UUID clinicId, UUID patientId) {
		patients.findById(patientId).filter(patient -> patient.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Patient not found"));
	}

	private void requireProfessional(UUID clinicId, UUID professionalId) {
		professionals.findById(professionalId).filter(professional -> professional.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Professional not found"));
	}

	private void requireExercise(UUID clinicId, UUID exerciseId) {
		exercises.findById(exerciseId).filter(exercise -> exercise.getClinicId().equals(clinicId))
				.filter(ExerciseEntity::isActive)
				.orElseThrow(() -> new NotFoundException("Exercise not found"));
	}

	public record CreateExercise(String name, String description, String videoUrl, String imageUrl) {
	}

	public record AssignExercisePlan(UUID professionalId, String title, String notes, LocalDate startsOn,
			List<AssignExerciseItem> items) {
	}

	public record AssignExerciseItem(UUID exerciseId, Integer series, Integer repetitions, String frequency,
			String notes) {
	}

	public record PlanWithItems(ExercisePlanEntity plan, List<ExercisePlanItemEntity> items) {
	}
}
