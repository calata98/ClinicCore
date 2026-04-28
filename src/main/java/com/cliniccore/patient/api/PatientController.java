package com.cliniccore.patient.api;

import com.cliniccore.patient.application.PatientService;
import com.cliniccore.patient.application.PatientService.UpsertPatient;
import com.cliniccore.patient.infrastructure.persistence.PatientEntity;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.application.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

	private final PatientService patientService;
	private final CurrentUserProvider currentUserProvider;

	public PatientController(PatientService patientService, CurrentUserProvider currentUserProvider) {
		this.patientService = patientService;
		this.currentUserProvider = currentUserProvider;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL','RECEPTIONIST')")
	PatientResponse create(@Valid @RequestBody PatientRequest request) {
		CurrentUser user = currentUserProvider.require();
		return PatientResponse.from(patientService.create(user, request.toCommand()));
	}

	@GetMapping
	List<PatientResponse> search(@RequestParam(required = false) String search) {
		return patientService.search(currentUserProvider.require().clinicId(), search).stream()
				.map(PatientResponse::from)
				.toList();
	}

	@GetMapping("/{patientId}")
	PatientResponse detail(@PathVariable UUID patientId) {
		return PatientResponse.from(patientService.detail(currentUserProvider.require(), patientId));
	}

	@PatchMapping("/{patientId}")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL','RECEPTIONIST')")
	PatientResponse update(@PathVariable UUID patientId, @Valid @RequestBody PatientRequest request) {
		CurrentUser user = currentUserProvider.require();
		return PatientResponse.from(patientService.update(user, patientId, request.toCommand()));
	}

	@DeleteMapping("/{patientId}")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN')")
	void deactivate(@PathVariable UUID patientId) {
		patientService.deactivate(currentUserProvider.require(), patientId);
	}

	record PatientRequest(@NotBlank String firstName, @NotBlank String lastName, String phone, @Email String email,
			LocalDate birthDate, String administrativeNotes, boolean consentAccepted) {

		UpsertPatient toCommand() {
			return new UpsertPatient(firstName, lastName, phone, email, birthDate, administrativeNotes, consentAccepted);
		}
	}

	record PatientResponse(UUID id, String firstName, String lastName, String phone, String email, LocalDate birthDate,
			String administrativeNotes, boolean consentAccepted, boolean active) {

		static PatientResponse from(PatientEntity patient) {
			return new PatientResponse(patient.getId(), patient.getFirstName(), patient.getLastName(), patient.getPhone(),
					patient.getEmail(), patient.getBirthDate(), patient.getAdministrativeNotes(),
					patient.isConsentAccepted(), patient.isActive());
		}
	}
}
