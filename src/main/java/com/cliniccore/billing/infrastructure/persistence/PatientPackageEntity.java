package com.cliniccore.billing.infrastructure.persistence;

import com.cliniccore.shared.infrastructure.persistence.MutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient_packages")
public class PatientPackageEntity extends MutableEntity {

	@Column(nullable = false)
	private UUID clinicId;

	@Column(nullable = false)
	private UUID patientId;

	@Column(nullable = false)
	private UUID packageId;

	@Column(nullable = false)
	private int totalSessions;

	@Column(nullable = false)
	private int remainingSessions;

	private BigDecimal paidAmount;

	@Column(length = 80)
	private String paymentMethod;

	@Column(nullable = false)
	private Instant purchasedAt;

	@Column(nullable = false)
	private boolean active = true;

	protected PatientPackageEntity() {
	}

	public PatientPackageEntity(UUID clinicId, UUID patientId, UUID packageId, int totalSessions, BigDecimal paidAmount,
			String paymentMethod) {
		this.clinicId = clinicId;
		this.patientId = patientId;
		this.packageId = packageId;
		this.totalSessions = totalSessions;
		this.remainingSessions = totalSessions;
		this.paidAmount = paidAmount;
		this.paymentMethod = paymentMethod;
		this.purchasedAt = Instant.now();
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public UUID getPackageId() {
		return packageId;
	}

	public int getTotalSessions() {
		return totalSessions;
	}

	public int getRemainingSessions() {
		return remainingSessions;
	}

	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public Instant getPurchasedAt() {
		return purchasedAt;
	}

	public boolean isActive() {
		return active;
	}

	public void consumeSession() {
		remainingSessions -= 1;
		if (remainingSessions == 0) {
			active = false;
		}
	}
}
