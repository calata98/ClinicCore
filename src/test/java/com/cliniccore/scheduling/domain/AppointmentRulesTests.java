package com.cliniccore.scheduling.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AppointmentRulesTests {

	@Test
	void detectsOverlappingRanges() {
		Instant ten = Instant.parse("2026-04-28T10:00:00Z");
		Instant eleven = Instant.parse("2026-04-28T11:00:00Z");
		Instant halfPastTen = Instant.parse("2026-04-28T10:30:00Z");
		Instant halfPastEleven = Instant.parse("2026-04-28T11:30:00Z");

		assertThat(AppointmentRules.overlaps(ten, eleven, halfPastTen, halfPastEleven)).isTrue();
	}

	@Test
	void adjacentRangesDoNotOverlap() {
		Instant ten = Instant.parse("2026-04-28T10:00:00Z");
		Instant eleven = Instant.parse("2026-04-28T11:00:00Z");
		Instant noon = Instant.parse("2026-04-28T12:00:00Z");

		assertThat(AppointmentRules.overlaps(ten, eleven, eleven, noon)).isFalse();
	}

	@Test
	void rejectsInvalidRange() {
		Instant ten = Instant.parse("2026-04-28T10:00:00Z");

		assertThatThrownBy(() -> AppointmentRules.requireValidRange(ten, ten))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
