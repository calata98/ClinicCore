package com.cliniccore.scheduling.domain;

import java.time.Instant;

public final class AppointmentRules {

	private AppointmentRules() {
	}

	public static void requireValidRange(Instant startAt, Instant endAt) {
		if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
			throw new IllegalArgumentException("Appointment start must be before end");
		}
	}

	public static boolean overlaps(Instant firstStart, Instant firstEnd, Instant secondStart, Instant secondEnd) {
		return firstStart.isBefore(secondEnd) && firstEnd.isAfter(secondStart);
	}
}
