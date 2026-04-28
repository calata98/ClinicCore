package com.cliniccore.billing.api;

import com.cliniccore.billing.application.PackageService;
import com.cliniccore.billing.application.PackageService.AssignPackage;
import com.cliniccore.billing.application.PackageService.CreatePackage;
import com.cliniccore.billing.infrastructure.persistence.PatientPackageEntity;
import com.cliniccore.billing.infrastructure.persistence.TreatmentPackageEntity;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.application.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PackageController {

	private final PackageService packageService;
	private final CurrentUserProvider currentUserProvider;

	public PackageController(PackageService packageService, CurrentUserProvider currentUserProvider) {
		this.packageService = packageService;
		this.currentUserProvider = currentUserProvider;
	}

	@PostMapping("/packages")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN')")
	TreatmentPackageResponse createPackage(@Valid @RequestBody CreatePackageRequest request) {
		CurrentUser user = currentUserProvider.require();
		return TreatmentPackageResponse.from(packageService.createPackage(user, request.toCommand()));
	}

	@GetMapping("/packages")
	List<TreatmentPackageResponse> listPackages() {
		return packageService.listPackages(currentUserProvider.require().clinicId()).stream()
				.map(TreatmentPackageResponse::from)
				.toList();
	}

	@PostMapping("/patients/{patientId}/packages")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','RECEPTIONIST')")
	PatientPackageResponse assignPackage(@PathVariable UUID patientId, @Valid @RequestBody AssignPackageRequest request) {
		CurrentUser user = currentUserProvider.require();
		return PatientPackageResponse.from(packageService.assignPackage(user, patientId, request.toCommand()));
	}

	@GetMapping("/patients/{patientId}/packages")
	List<PatientPackageResponse> listPatientPackages(@PathVariable UUID patientId) {
		return packageService.listPatientPackages(currentUserProvider.require().clinicId(), patientId).stream()
				.map(PatientPackageResponse::from)
				.toList();
	}

	@PatchMapping("/patient-packages/{patientPackageId}/consume-session")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL','RECEPTIONIST')")
	PatientPackageResponse consumeSession(@PathVariable UUID patientPackageId) {
		return PatientPackageResponse.from(packageService.consumeSession(currentUserProvider.require(), patientPackageId));
	}

	record CreatePackageRequest(@NotBlank String name, @Positive int totalSessions, BigDecimal price) {

		CreatePackage toCommand() {
			return new CreatePackage(name, totalSessions, price);
		}
	}

	record AssignPackageRequest(@NotNull UUID packageId, BigDecimal paidAmount, String paymentMethod) {

		AssignPackage toCommand() {
			return new AssignPackage(packageId, paidAmount, paymentMethod);
		}
	}

	record TreatmentPackageResponse(UUID id, String name, int totalSessions, BigDecimal price) {

		static TreatmentPackageResponse from(TreatmentPackageEntity treatmentPackage) {
			return new TreatmentPackageResponse(treatmentPackage.getId(), treatmentPackage.getName(),
					treatmentPackage.getTotalSessions(), treatmentPackage.getPrice());
		}
	}

	record PatientPackageResponse(UUID id, UUID patientId, UUID packageId, int totalSessions, int remainingSessions,
			BigDecimal paidAmount, String paymentMethod, Instant purchasedAt, boolean active) {

		static PatientPackageResponse from(PatientPackageEntity patientPackage) {
			return new PatientPackageResponse(patientPackage.getId(), patientPackage.getPatientId(),
					patientPackage.getPackageId(), patientPackage.getTotalSessions(),
					patientPackage.getRemainingSessions(), patientPackage.getPaidAmount(),
					patientPackage.getPaymentMethod(), patientPackage.getPurchasedAt(), patientPackage.isActive());
		}
	}
}
