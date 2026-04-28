package com.cliniccore.treatment.infrastructure.persistence;

import com.cliniccore.treatment.domain.EpisodeStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreatmentEpisodeJpaRepository extends JpaRepository<TreatmentEpisodeEntity, UUID> {

	List<TreatmentEpisodeEntity> findByClinicIdAndPatientIdOrderByStartDateDesc(UUID clinicId, UUID patientId);

	long countByClinicIdAndStatus(UUID clinicId, EpisodeStatus status);
}
