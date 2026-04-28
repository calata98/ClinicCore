package com.cliniccore.audit.infrastructure.persistence;

import com.cliniccore.audit.domain.AuditAction;
import com.cliniccore.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity extends BaseEntity {

	private UUID clinicId;

	private UUID actorUserId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 80)
	private AuditAction action;

	@Column(nullable = false, length = 120)
	private String resourceType;

	private UUID resourceId;

	@Column(length = 2000)
	private String details;

	@Column(nullable = false)
	private Instant occurredAt;

	protected AuditLogEntity() {
	}

	public AuditLogEntity(UUID clinicId, UUID actorUserId, AuditAction action, String resourceType, UUID resourceId,
			String details) {
		this.clinicId = clinicId;
		this.actorUserId = actorUserId;
		this.action = action;
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		this.details = details;
	}

	@PrePersist
	void prePersistAudit() {
		if (occurredAt == null) {
			occurredAt = Instant.now();
		}
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public UUID getActorUserId() {
		return actorUserId;
	}

	public AuditAction getAction() {
		return action;
	}

	public String getResourceType() {
		return resourceType;
	}

	public UUID getResourceId() {
		return resourceId;
	}

	public String getDetails() {
		return details;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}
}
