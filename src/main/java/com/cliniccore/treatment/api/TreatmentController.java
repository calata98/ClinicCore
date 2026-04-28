package com.cliniccore.treatment.api;

import com.cliniccore.shared.application.CurrentUser;
import com.cliniccore.shared.application.CurrentUserProvider;
import com.cliniccore.treatment.application.TreatmentService;
import com.cliniccore.treatment.application.TreatmentService.AddSessionNote;
import com.cliniccore.treatment.application.TreatmentService.OpenEpisode;
import com.cliniccore.treatment.application.TreatmentService.PatientTimeline;
import com.cliniccore.treatment.domain.EpisodeStatus;
import com.cliniccore.treatment.infrastructure.persistence.SessionNoteEntity;
import com.cliniccore.treatment.infrastructure.persistence.TreatmentEpisodeEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
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
public class TreatmentController {

	private final TreatmentService treatmentService;
	private final CurrentUserProvider currentUserProvider;

	public TreatmentController(TreatmentService treatmentService, CurrentUserProvider currentUserProvider) {
		this.treatmentService = treatmentService;
		this.currentUserProvider = currentUserProvider;
	}

	@PostMapping("/patients/{patientId}/episodes")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL')")
	EpisodeResponse openEpisode(@PathVariable UUID patientId, @Valid @RequestBody OpenEpisodeRequest request) {
		CurrentUser user = currentUserProvider.require();
		return EpisodeResponse.from(treatmentService.openEpisode(user, patientId, request.toCommand()));
	}

	@PatchMapping("/episodes/{episodeId}/close")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL')")
	EpisodeResponse closeEpisode(@PathVariable UUID episodeId, @RequestBody CloseEpisodeRequest request) {
		return EpisodeResponse.from(treatmentService.closeEpisode(currentUserProvider.require(), episodeId,
				request == null ? null : request.endDate()));
	}

	@PostMapping("/episodes/{episodeId}/session-notes")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL')")
	SessionNoteResponse addSessionNote(@PathVariable UUID episodeId, @Valid @RequestBody SessionNoteRequest request) {
		return SessionNoteResponse.from(treatmentService.addSessionNote(currentUserProvider.require(), episodeId,
				request.toCommand()));
	}

	@GetMapping("/patients/{patientId}/timeline")
	@PreAuthorize("hasAnyRole('OWNER','ADMIN','PROFESSIONAL')")
	TimelineResponse timeline(@PathVariable UUID patientId) {
		return TimelineResponse.from(treatmentService.timeline(currentUserProvider.require(), patientId));
	}

	record OpenEpisodeRequest(@NotNull UUID responsibleProfessionalId, @NotBlank String title,
			@NotNull LocalDate startDate) {

		OpenEpisode toCommand() {
			return new OpenEpisode(responsibleProfessionalId, title, startDate);
		}
	}

	record CloseEpisodeRequest(LocalDate endDate) {
	}

	record SessionNoteRequest(UUID appointmentId, @NotNull UUID professionalId, @NotNull LocalDate sessionDate,
			Integer painLevel, String treatedArea, String techniquesApplied, String observations,
			String nextRecommendation) {

		AddSessionNote toCommand() {
			return new AddSessionNote(appointmentId, professionalId, sessionDate, painLevel, treatedArea,
					techniquesApplied, observations, nextRecommendation);
		}
	}

	record EpisodeResponse(UUID id, UUID patientId, UUID responsibleProfessionalId, String title, LocalDate startDate,
			LocalDate endDate, EpisodeStatus status) {

		static EpisodeResponse from(TreatmentEpisodeEntity episode) {
			return new EpisodeResponse(episode.getId(), episode.getPatientId(), episode.getResponsibleProfessionalId(),
					episode.getTitle(), episode.getStartDate(), episode.getEndDate(), episode.getStatus());
		}
	}

	record SessionNoteResponse(UUID id, UUID episodeId, UUID appointmentId, UUID professionalId, LocalDate sessionDate,
			Integer painLevel, String treatedArea, String techniquesApplied, String observations,
			String nextRecommendation) {

		static SessionNoteResponse from(SessionNoteEntity note) {
			return new SessionNoteResponse(note.getId(), note.getEpisodeId(), note.getAppointmentId(),
					note.getProfessionalId(), note.getSessionDate(), note.getPainLevel(), note.getTreatedArea(),
					note.getTechniquesApplied(), note.getObservations(), note.getNextRecommendation());
		}
	}

	record TimelineResponse(UUID patientId, List<EpisodeLineResponse> episodes) {

		static TimelineResponse from(PatientTimeline timeline) {
			return new TimelineResponse(timeline.patientId(), timeline.episodes().stream()
					.map(line -> new EpisodeLineResponse(EpisodeResponse.from(line.episode()),
							line.notes().stream().map(SessionNoteResponse::from).toList()))
					.toList());
		}
	}

	record EpisodeLineResponse(EpisodeResponse episode, List<SessionNoteResponse> notes) {
	}
}
