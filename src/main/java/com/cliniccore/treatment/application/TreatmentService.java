package com.cliniccore.treatment.application;

import com.cliniccore.audit.application.AuditService;
import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.clinic.infrastructure.persistence.ProfessionalJpaRepository;
import com.cliniccore.patient.infrastructure.persistence.PatientJpaRepository;
import com.cliniccore.scheduling.infrastructure.persistence.AppointmentJpaRepository;
import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.domain.ConflictException;
import com.cliniccore.shared.domain.NotFoundException;
import com.cliniccore.treatment.domain.EpisodeStatus;
import com.cliniccore.treatment.infrastructure.persistence.SessionNoteEntity;
import com.cliniccore.treatment.infrastructure.persistence.SessionNoteJpaRepository;
import com.cliniccore.treatment.infrastructure.persistence.TreatmentEpisodeEntity;
import com.cliniccore.treatment.infrastructure.persistence.TreatmentEpisodeJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TreatmentService {

	private final TreatmentEpisodeJpaRepository episodes;
	private final SessionNoteJpaRepository notes;
	private final PatientJpaRepository patients;
	private final ProfessionalJpaRepository professionals;
	private final AppointmentJpaRepository appointments;
	private final AuditService auditService;

	public TreatmentService(TreatmentEpisodeJpaRepository episodes, SessionNoteJpaRepository notes,
			PatientJpaRepository patients, ProfessionalJpaRepository professionals, AppointmentJpaRepository appointments,
			AuditService auditService) {
		this.episodes = episodes;
		this.notes = notes;
		this.patients = patients;
		this.professionals = professionals;
		this.appointments = appointments;
		this.auditService = auditService;
	}

	@Transactional
	public TreatmentEpisodeEntity openEpisode(CurrentUser user, UUID patientId, OpenEpisode command) {
		requirePatient(user.clinicId(), patientId);
		requireProfessional(user.clinicId(), command.responsibleProfessionalId());
		TreatmentEpisodeEntity episode = episodes.save(new TreatmentEpisodeEntity(user.clinicId(), patientId,
				command.responsibleProfessionalId(), command.title(), command.startDate()));
		auditService.record(user, AuditAction.OPEN_EPISODE, "TreatmentEpisode", episode.getId(),
				"Treatment episode opened");
		return episode;
	}

	@Transactional
	public TreatmentEpisodeEntity closeEpisode(CurrentUser user, UUID episodeId, LocalDate endDate) {
		TreatmentEpisodeEntity episode = findEpisode(user.clinicId(), episodeId);
		if (episode.getStatus() == EpisodeStatus.CLOSED) {
			throw new ConflictException("Treatment episode is already closed");
		}
		episode.close(endDate == null ? LocalDate.now() : endDate);
		auditService.record(user, AuditAction.CLOSE_EPISODE, "TreatmentEpisode", episodeId,
				"Treatment episode closed");
		return episode;
	}

	@Transactional
	public SessionNoteEntity addSessionNote(CurrentUser user, UUID episodeId, AddSessionNote command) {
		TreatmentEpisodeEntity episode = findEpisode(user.clinicId(), episodeId);
		if (episode.getStatus() != EpisodeStatus.OPEN) {
			throw new ConflictException("Cannot add notes to a closed episode");
		}
		requireProfessional(user.clinicId(), command.professionalId());
		if (command.appointmentId() != null) {
			appointments.findById(command.appointmentId())
					.filter(appointment -> appointment.getClinicId().equals(user.clinicId()))
					.orElseThrow(() -> new NotFoundException("Appointment not found"));
		}
		if (command.painLevel() != null && (command.painLevel() < 0 || command.painLevel() > 10)) {
			throw new IllegalArgumentException("Pain level must be between 0 and 10");
		}
		SessionNoteEntity note = notes.save(new SessionNoteEntity(user.clinicId(), episodeId, command.appointmentId(),
				command.professionalId(), command.sessionDate(), command.painLevel(), command.treatedArea(),
				command.techniquesApplied(), command.observations(), command.nextRecommendation()));
		auditService.record(user, AuditAction.ADD_SESSION_NOTE, "SessionNote", note.getId(), "Session note added");
		return note;
	}

	@Transactional
	public PatientTimeline timeline(CurrentUser user, UUID patientId) {
		requirePatient(user.clinicId(), patientId);
		List<EpisodeWithNotes> episodeLines = episodes.findByClinicIdAndPatientIdOrderByStartDateDesc(user.clinicId(),
				patientId).stream()
				.map(episode -> new EpisodeWithNotes(episode,
						notes.findByClinicIdAndEpisodeIdOrderBySessionDateDesc(user.clinicId(), episode.getId())))
				.toList();
		auditService.record(user, AuditAction.READ_PATIENT, "Patient", patientId, "Clinical timeline read");
		return new PatientTimeline(patientId, episodeLines);
	}

	private TreatmentEpisodeEntity findEpisode(UUID clinicId, UUID episodeId) {
		return episodes.findById(episodeId)
				.filter(episode -> episode.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Treatment episode not found"));
	}

	private void requirePatient(UUID clinicId, UUID patientId) {
		patients.findById(patientId).filter(patient -> patient.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Patient not found"));
	}

	private void requireProfessional(UUID clinicId, UUID professionalId) {
		professionals.findById(professionalId).filter(professional -> professional.getClinicId().equals(clinicId))
				.orElseThrow(() -> new NotFoundException("Professional not found"));
	}

	public record OpenEpisode(UUID responsibleProfessionalId, String title, LocalDate startDate) {
	}

	public record AddSessionNote(UUID appointmentId, UUID professionalId, LocalDate sessionDate, Integer painLevel,
			String treatedArea, String techniquesApplied, String observations, String nextRecommendation) {
	}

	public record EpisodeWithNotes(TreatmentEpisodeEntity episode, List<SessionNoteEntity> notes) {
	}

	public record PatientTimeline(UUID patientId, List<EpisodeWithNotes> episodes) {
	}
}
