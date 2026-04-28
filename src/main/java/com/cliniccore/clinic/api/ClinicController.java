package com.cliniccore.clinic.api;

import com.cliniccore.clinic.application.ClinicManagementService;
import com.cliniccore.clinic.application.ClinicManagementService.CreateClinic;
import com.cliniccore.clinic.application.ClinicManagementService.CreateProfessional;
import com.cliniccore.clinic.application.ClinicManagementService.CreateService;
import com.cliniccore.clinic.infrastructure.persistence.ClinicEntity;
import com.cliniccore.clinic.infrastructure.persistence.ClinicServiceEntity;
import com.cliniccore.clinic.infrastructure.persistence.ProfessionalEntity;
import com.cliniccore.clinic.infrastructure.persistence.RoomEntity;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.application.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ClinicController {

	private final ClinicManagementService clinicService;
	private final CurrentUserProvider currentUserProvider;

	public ClinicController(ClinicManagementService clinicService, CurrentUserProvider currentUserProvider) {
		this.clinicService = clinicService;
		this.currentUserProvider = currentUserProvider;
	}

	@GetMapping("/clinics/current")
	ClinicResponse currentClinic() {
		return ClinicResponse.from(clinicService.currentClinic(currentUserProvider.require()));
	}

	@PostMapping("/clinics")
	@PreAuthorize("hasRole('OWNER')")
	ClinicResponse createClinic(@Valid @RequestBody CreateClinicRequest request) {
		CurrentUser user = currentUserProvider.require();
		return ClinicResponse.from(clinicService.createClinic(user,
				new CreateClinic(request.name(), request.legalName(), request.phone(), request.email())));
	}

	@PostMapping("/professionals")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN')")
	ProfessionalResponse createProfessional(@Valid @RequestBody CreateProfessionalRequest request) {
		CurrentUser user = currentUserProvider.require();
		return ProfessionalResponse.from(clinicService.createProfessional(user,
				new CreateProfessional(request.userId(), request.firstName(), request.lastName(), request.email(),
						request.phone(), request.color())));
	}

	@GetMapping("/professionals")
	List<ProfessionalResponse> listProfessionals() {
		return clinicService.listProfessionals(currentUserProvider.require().clinicId()).stream()
				.map(ProfessionalResponse::from)
				.toList();
	}

	@PostMapping("/rooms")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN')")
	RoomResponse createRoom(@Valid @RequestBody CreateRoomRequest request) {
		CurrentUser user = currentUserProvider.require();
		return RoomResponse.from(clinicService.createRoom(user, request.name()));
	}

	@GetMapping("/rooms")
	List<RoomResponse> listRooms() {
		return clinicService.listRooms(currentUserProvider.require().clinicId()).stream().map(RoomResponse::from).toList();
	}

	@PostMapping("/services")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN')")
	ServiceResponse createService(@Valid @RequestBody CreateServiceRequest request) {
		CurrentUser user = currentUserProvider.require();
		return ServiceResponse.from(clinicService.createService(user,
				new CreateService(request.name(), request.durationMinutes(), request.price())));
	}

	@GetMapping("/services")
	List<ServiceResponse> listServices() {
		return clinicService.listServices(currentUserProvider.require().clinicId()).stream().map(ServiceResponse::from)
				.toList();
	}

	record CreateClinicRequest(@NotBlank String name, String legalName, String phone, @Email String email) {
	}

	record CreateProfessionalRequest(UUID userId, @NotBlank String firstName, @NotBlank String lastName,
			@Email String email, String phone, String color) {
	}

	record CreateRoomRequest(@NotBlank String name) {
	}

	record CreateServiceRequest(@NotBlank String name, @Positive int durationMinutes, BigDecimal price) {
	}

	record ClinicResponse(UUID id, String name, String legalName, String phone, String email) {
		static ClinicResponse from(ClinicEntity clinic) {
			return new ClinicResponse(clinic.getId(), clinic.getName(), clinic.getLegalName(), clinic.getPhone(),
					clinic.getEmail());
		}
	}

	record ProfessionalResponse(UUID id, String firstName, String lastName, String email, String phone, String color) {
		static ProfessionalResponse from(ProfessionalEntity professional) {
			return new ProfessionalResponse(professional.getId(), professional.getFirstName(),
					professional.getLastName(), professional.getEmail(), professional.getPhone(),
					professional.getColor());
		}
	}

	record RoomResponse(UUID id, String name) {
		static RoomResponse from(RoomEntity room) {
			return new RoomResponse(room.getId(), room.getName());
		}
	}

	record ServiceResponse(UUID id, String name, int durationMinutes, BigDecimal price) {
		static ServiceResponse from(ClinicServiceEntity service) {
			return new ServiceResponse(service.getId(), service.getName(), service.getDurationMinutes(),
					service.getPrice());
		}
	}
}
