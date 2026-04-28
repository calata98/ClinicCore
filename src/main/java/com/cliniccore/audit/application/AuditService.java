package com.cliniccore.audit.application;

import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.audit.infrastructure.persistence.AuditLogEntity;
import com.cliniccore.audit.infrastructure.persistence.AuditLogJpaRepository;
import com.cliniccore.shared.application.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

	private final AuditLogJpaRepository auditLogs;

	public AuditService(AuditLogJpaRepository auditLogs) {
		this.auditLogs = auditLogs;
	}

	@Transactional
	public void record(CurrentUser user, AuditAction action, String resourceType, UUID resourceId, String details) {
		record(user.clinicId(), user.userId(), action, resourceType, resourceId, details);
	}

	@Transactional
	public void record(UUID clinicId, UUID actorUserId, AuditAction action, String resourceType, UUID resourceId,
			String details) {
		auditLogs.save(new AuditLogEntity(clinicId, actorUserId, action, resourceType, resourceId, details));
	}

	@Transactional(readOnly = true)
	public List<AuditLogEntity> recent(UUID clinicId) {
		return auditLogs.findTop100ByClinicIdOrderByOccurredAtDesc(clinicId);
	}
}
