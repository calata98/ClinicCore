package com.cliniccore.identity.application;

import com.cliniccore.billing.infrastructure.persistence.TreatmentPackageEntity;
import com.cliniccore.billing.infrastructure.persistence.TreatmentPackageJpaRepository;
import com.cliniccore.clinic.infrastructure.persistence.ClinicEntity;
import com.cliniccore.clinic.infrastructure.persistence.ClinicJpaRepository;
import com.cliniccore.clinic.infrastructure.persistence.ClinicServiceEntity;
import com.cliniccore.clinic.infrastructure.persistence.ClinicServiceJpaRepository;
import com.cliniccore.clinic.infrastructure.persistence.ProfessionalEntity;
import com.cliniccore.clinic.infrastructure.persistence.ProfessionalJpaRepository;
import com.cliniccore.clinic.infrastructure.persistence.RoomEntity;
import com.cliniccore.clinic.infrastructure.persistence.RoomJpaRepository;
import com.cliniccore.exercise.infrastructure.persistence.ExerciseEntity;
import com.cliniccore.exercise.infrastructure.persistence.ExerciseJpaRepository;
import com.cliniccore.identity.domain.Role;
import com.cliniccore.identity.infrastructure.persistence.UserAccountEntity;
import com.cliniccore.identity.infrastructure.persistence.UserAccountJpaRepository;
import com.cliniccore.patient.infrastructure.persistence.PatientEntity;
import com.cliniccore.patient.infrastructure.persistence.PatientJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapDemoData implements ApplicationRunner {

	private final boolean enabled;
	private final ClinicJpaRepository clinics;
	private final UserAccountJpaRepository users;
	private final ProfessionalJpaRepository professionals;
	private final RoomJpaRepository rooms;
	private final ClinicServiceJpaRepository services;
	private final PatientJpaRepository patients;
	private final TreatmentPackageJpaRepository packages;
	private final ExerciseJpaRepository exercises;
	private final PasswordEncoder passwordEncoder;

	public BootstrapDemoData(@Value("${cliniccore.bootstrap-demo:false}") boolean enabled, ClinicJpaRepository clinics,
			UserAccountJpaRepository users, ProfessionalJpaRepository professionals, RoomJpaRepository rooms,
			ClinicServiceJpaRepository services, PatientJpaRepository patients, TreatmentPackageJpaRepository packages,
			ExerciseJpaRepository exercises, PasswordEncoder passwordEncoder) {
		this.enabled = enabled;
		this.clinics = clinics;
		this.users = users;
		this.professionals = professionals;
		this.rooms = rooms;
		this.services = services;
		this.patients = patients;
		this.packages = packages;
		this.exercises = exercises;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!enabled || users.existsByEmailIgnoreCase("owner@cliniccore.local")) {
			return;
		}
		ClinicEntity clinic = clinics.save(new ClinicEntity("Fisio Norte", "Fisio Norte S.L.", "+34 600 000 000",
				"hola@fisionorte.local"));
		users.save(new UserAccountEntity(clinic.getId(), "owner@cliniccore.local",
				passwordEncoder.encode("ChangeMe123!"), "Owner Demo", Role.OWNER));
		ProfessionalEntity ana = professionals.save(new ProfessionalEntity(clinic.getId(), null, "Ana", "Lopez",
				"ana@fisionorte.local", "+34 600 000 001", "#2f80ed"));
		professionals.save(new ProfessionalEntity(clinic.getId(), null, "Marcos", "Garcia", "marcos@fisionorte.local",
				"+34 600 000 002", "#27ae60"));
		rooms.save(new RoomEntity(clinic.getId(), "Sala 1"));
		rooms.save(new RoomEntity(clinic.getId(), "Sala 2"));
		services.save(new ClinicServiceEntity(clinic.getId(), "Fisioterapia 45 min", 45, new BigDecimal("45.00")));
		services.save(new ClinicServiceEntity(clinic.getId(), "Primera valoracion", 60, new BigDecimal("60.00")));
		patients.save(new PatientEntity(clinic.getId(), "Juan", "Perez", "+34 611 111 111", "juan@example.com",
				LocalDate.of(1988, 3, 12), "Paciente demo para probar el MVP", true));
		packages.save(new TreatmentPackageEntity(clinic.getId(), "Bono 5 sesiones", 5, new BigDecimal("200.00")));
		packages.save(new TreatmentPackageEntity(clinic.getId(), "Bono 10 sesiones", 10, new BigDecimal("380.00")));
		exercises.save(new ExerciseEntity(clinic.getId(), "Puente de gluteo",
				"Fortalecimiento de cadena posterior sin provocar dolor.", null, null));
		exercises.save(new ExerciseEntity(clinic.getId(), "Movilidad de cadera",
				"Ejercicio suave de movilidad para preparar la sesion.", null, null));
	}
}
