package com.cliniccore.analytics.application;

import com.cliniccore.audit.application.AuditService;
import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.billing.infrastructure.persistence.PatientPackageJpaRepository;
import com.cliniccore.patient.infrastructure.persistence.PatientJpaRepository;
import com.cliniccore.scheduling.domain.AppointmentStatus;
import com.cliniccore.scheduling.infrastructure.persistence.AppointmentJpaRepository;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.treatment.domain.EpisodeStatus;
import com.cliniccore.treatment.infrastructure.persistence.TreatmentEpisodeJpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

	private final AppointmentJpaRepository appointments;
	private final PatientJpaRepository patients;
	private final TreatmentEpisodeJpaRepository episodes;
	private final PatientPackageJpaRepository patientPackages;
	private final AuditService auditService;

	public DashboardService(AppointmentJpaRepository appointments, PatientJpaRepository patients,
			TreatmentEpisodeJpaRepository episodes, PatientPackageJpaRepository patientPackages,
			AuditService auditService) {
		this.appointments = appointments;
		this.patients = patients;
		this.episodes = episodes;
		this.patientPackages = patientPackages;
		this.auditService = auditService;
	}

	@Transactional
	public ClinicDashboard clinicDashboard(CurrentUser user) {
		Instant now = Instant.now();
		Instant inSevenDays = now.plus(7, ChronoUnit.DAYS);
		ClinicDashboard dashboard = new ClinicDashboard(
				appointments.countByClinicIdAndStartAtBetween(user.clinicId(), now, inSevenDays),
				appointments.countByClinicIdAndStatusAndStartAtBetween(user.clinicId(), AppointmentStatus.CANCELLED, now,
						inSevenDays),
				appointments.countByClinicIdAndStatusAndStartAtBetween(user.clinicId(), AppointmentStatus.NO_SHOW,
						now.minus(30, ChronoUnit.DAYS), now),
				patients.countByClinicIdAndActiveTrue(user.clinicId()),
				episodes.countByClinicIdAndStatus(user.clinicId(), EpisodeStatus.OPEN),
				patientPackages.countByClinicIdAndActiveTrue(user.clinicId()),
				patientPackages.sumPaidAmountByClinicId(user.clinicId()));
		auditService.record(user, AuditAction.READ_DASHBOARD, "Dashboard", null, "Clinic dashboard read");
		return dashboard;
	}

	public record ClinicDashboard(long appointmentsNextSevenDays, long cancelledNextSevenDays, long noShowsLastThirtyDays,
			long activePatients, long openEpisodes, long activePackages, BigDecimal registeredIncome) {
	}
}
