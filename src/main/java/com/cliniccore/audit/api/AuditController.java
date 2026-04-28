package com.cliniccore.audit.api;

import com.cliniccore.audit.application.AuditService;
import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.audit.infrastructure.persistence.AuditLogEntity;
import com.cliniccore.shared.application.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditController {

	private final AuditService auditService;
	private final CurrentUserProvider currentUserProvider;

	public AuditController(AuditService auditService, CurrentUserProvider currentUserProvider) {
		this.auditService = auditService;
		this.currentUserProvider = currentUserProvider;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('OWNER','ADMIN')")
	List<AuditLogResponse> recent() {
		return auditService.recent(currentUserProvider.require().clinicId()).stream().map(AuditLogResponse::from)
				.toList();
	}

	record AuditLogResponse(UUID id, UUID clinicId, UUID actorUserId, AuditAction action, String resourceType,
			UUID resourceId, String details, Instant occurredAt) {

		static AuditLogResponse from(AuditLogEntity auditLog) {
			return new AuditLogResponse(auditLog.getId(), auditLog.getClinicId(), auditLog.getActorUserId(),
					auditLog.getAction(), auditLog.getResourceType(), auditLog.getResourceId(), auditLog.getDetails(),
					auditLog.getOccurredAt());
		}
	}
}
