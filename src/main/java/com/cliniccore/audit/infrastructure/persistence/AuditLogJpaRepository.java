package com.cliniccore.audit.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, UUID> {

	List<AuditLogEntity> findTop100ByClinicIdOrderByOccurredAtDesc(UUID clinicId);
}
