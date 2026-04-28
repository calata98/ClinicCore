package com.cliniccore.treatment.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionNoteJpaRepository extends JpaRepository<SessionNoteEntity, UUID> {

	List<SessionNoteEntity> findByClinicIdAndEpisodeIdOrderBySessionDateDesc(UUID clinicId, UUID episodeId);
}
