package com.cliniccore.scheduling.api;

import com.cliniccore.scheduling.application.AppointmentService;
import com.cliniccore.scheduling.application.AppointmentService.RescheduleAppointment;
import com.cliniccore.scheduling.application.AppointmentService.ScheduleAppointment;
import com.cliniccore.scheduling.domain.AppointmentStatus;
import com.cliniccore.scheduling.infrastructure.persistence.AppointmentEntity;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.application.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

	private final AppointmentService appointmentService;
	private final CurrentUserProvider currentUserProvider;

	public AppointmentController(AppointmentService appointmentService, CurrentUserProvider currentUserProvider) {
		this.appointmentService = appointmentService;
		this.currentUserProvider = currentUserProvider;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL','RECEPTIONIST')")
	AppointmentResponse create(@Valid @RequestBody CreateAppointmentRequest request) {
		CurrentUser user = currentUserProvider.require();
		return AppointmentResponse.from(appointmentService.create(user, request.toCommand()));
	}

	@GetMapping
	List<AppointmentResponse> between(@RequestParam Instant from, @RequestParam Instant to) {
		return appointmentService.between(currentUserProvider.require().clinicId(), from, to).stream()
				.map(AppointmentResponse::from)
				.toList();
	}

	@PatchMapping("/{appointmentId}/reschedule")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL','RECEPTIONIST')")
	AppointmentResponse reschedule(@PathVariable UUID appointmentId, @Valid @RequestBody RescheduleRequest request) {
		CurrentUser user = currentUserProvider.require();
		return AppointmentResponse.from(appointmentService.reschedule(user, appointmentId, request.toCommand()));
	}

	@PatchMapping("/{appointmentId}/cancel")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL','RECEPTIONIST')")
	AppointmentResponse cancel(@PathVariable UUID appointmentId, @RequestBody CancelRequest request) {
		return AppointmentResponse.from(appointmentService.cancel(currentUserProvider.require(), appointmentId,
				request == null ? null : request.reason()));
	}

	@PatchMapping("/{appointmentId}/complete")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL')")
	AppointmentResponse complete(@PathVariable UUID appointmentId) {
		return AppointmentResponse.from(appointmentService.complete(currentUserProvider.require(), appointmentId));
	}

	@PatchMapping("/{appointmentId}/no-show")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL','RECEPTIONIST')")
	AppointmentResponse noShow(@PathVariable UUID appointmentId) {
		return AppointmentResponse.from(appointmentService.markNoShow(currentUserProvider.require(), appointmentId));
	}

	record CreateAppointmentRequest(@NotNull UUID patientId, @NotNull UUID professionalId, @NotNull UUID roomId,
			@NotNull UUID serviceId, @NotNull Instant startAt, @NotNull Instant endAt, String reason) {

		ScheduleAppointment toCommand() {
			return new ScheduleAppointment(patientId, professionalId, roomId, serviceId, startAt, endAt, reason);
		}
	}

	record RescheduleRequest(@NotNull UUID professionalId, @NotNull UUID roomId, @NotNull Instant startAt,
			@NotNull Instant endAt) {

		RescheduleAppointment toCommand() {
			return new RescheduleAppointment(professionalId, roomId, startAt, endAt);
		}
	}

	record CancelRequest(String reason) {
	}

	record AppointmentResponse(UUID id, UUID patientId, UUID professionalId, UUID roomId, UUID serviceId,
			Instant startAt, Instant endAt, AppointmentStatus status, String reason, String cancellationReason) {

		static AppointmentResponse from(AppointmentEntity appointment) {
			return new AppointmentResponse(appointment.getId(), appointment.getPatientId(),
					appointment.getProfessionalId(), appointment.getRoomId(), appointment.getServiceId(),
					appointment.getStartAt(), appointment.getEndAt(), appointment.getStatus(), appointment.getReason(),
					appointment.getCancellationReason());
		}
	}
}
