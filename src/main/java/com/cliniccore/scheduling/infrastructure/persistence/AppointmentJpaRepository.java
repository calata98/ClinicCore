package com.cliniccore.scheduling.infrastructure.persistence;

import com.cliniccore.scheduling.domain.AppointmentStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID> {

	List<AppointmentEntity> findByClinicIdAndStartAtBetweenOrderByStartAtAsc(UUID clinicId, Instant from, Instant to);

	long countByClinicIdAndStartAtBetween(UUID clinicId, Instant from, Instant to);

	long countByClinicIdAndStatusAndStartAtBetween(UUID clinicId, AppointmentStatus status, Instant from, Instant to);

	@Query("""
			select count(a) > 0
			from AppointmentEntity a
			where a.clinicId = :clinicId
			  and a.professionalId = :professionalId
			  and a.status not in :excludedStatuses
			  and (:ignoredAppointmentId is null or a.id <> :ignoredAppointmentId)
			  and a.startAt < :endAt
			  and a.endAt > :startAt
			""")
	boolean existsProfessionalOverlap(@Param("clinicId") UUID clinicId, @Param("professionalId") UUID professionalId,
			@Param("startAt") Instant startAt, @Param("endAt") Instant endAt,
			@Param("ignoredAppointmentId") UUID ignoredAppointmentId,
			@Param("excludedStatuses") Collection<AppointmentStatus> excludedStatuses);

	@Query("""
			select count(a) > 0
			from AppointmentEntity a
			where a.clinicId = :clinicId
			  and a.roomId = :roomId
			  and a.status not in :excludedStatuses
			  and (:ignoredAppointmentId is null or a.id <> :ignoredAppointmentId)
			  and a.startAt < :endAt
			  and a.endAt > :startAt
			""")
	boolean existsRoomOverlap(@Param("clinicId") UUID clinicId, @Param("roomId") UUID roomId,
			@Param("startAt") Instant startAt, @Param("endAt") Instant endAt,
			@Param("ignoredAppointmentId") UUID ignoredAppointmentId,
			@Param("excludedStatuses") Collection<AppointmentStatus> excludedStatuses);
}
