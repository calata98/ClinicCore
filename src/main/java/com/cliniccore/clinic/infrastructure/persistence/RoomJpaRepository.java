package com.cliniccore.clinic.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomJpaRepository extends JpaRepository<RoomEntity, UUID> {

	List<RoomEntity> findByClinicIdAndActiveTrueOrderByNameAsc(UUID clinicId);
}
