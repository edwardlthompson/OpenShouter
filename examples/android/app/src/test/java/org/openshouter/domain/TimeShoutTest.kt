package org.openshouter.domain

import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeShoutTest {
    private val utc = ZoneOffset.UTC

    @Test
    fun nextHourFromQuarterPast() {
        val now = OffsetDateTime.of(2026, 8, 13, 14, 17, 5, 0, utc).toInstant().toEpochMilli()
        val next = TimeShout.nextTriggerMillis(now, TimeShout.INTERVAL_HOUR, utc)
        val expected = OffsetDateTime.of(2026, 8, 13, 15, 0, 0, 0, utc).toInstant().toEpochMilli()
        assertEquals(expected, next)
    }

    @Test
    fun nextQuarterFromSeventeen() {
        val now = OffsetDateTime.of(2026, 8, 13, 14, 17, 0, 0, utc).toInstant().toEpochMilli()
        val next = TimeShout.nextTriggerMillis(now, TimeShout.INTERVAL_QUARTER, utc)
        val expected = OffsetDateTime.of(2026, 8, 13, 14, 30, 0, 0, utc).toInstant().toEpochMilli()
        assertEquals(expected, next)
    }

    @Test
    fun midnightRollover() {
        val now = OffsetDateTime.of(2026, 8, 13, 23, 50, 0, 0, utc).toInstant().toEpochMilli()
        val next = TimeShout.nextTriggerMillis(now, TimeShout.INTERVAL_HOUR, utc)
        val expected = OffsetDateTime.of(2026, 8, 14, 0, 0, 0, 0, utc).toInstant().toEpochMilli()
        assertEquals(expected, next)
    }

    @Test
    fun exactBoundaryGoesToNextSlot() {
        val now = OffsetDateTime.of(2026, 8, 13, 15, 0, 0, 0, utc).toInstant().toEpochMilli()
        val next = TimeShout.nextTriggerMillis(now, TimeShout.INTERVAL_HOUR, utc)
        val expected = OffsetDateTime.of(2026, 8, 13, 16, 0, 0, 0, utc).toInstant().toEpochMilli()
        assertEquals(expected, next)
    }

    @Test
    fun invalidIntervalBecomesHour() {
        assertEquals(TimeShout.INTERVAL_HOUR, TimeShout.normalizeInterval(0))
        assertEquals(TimeShout.INTERVAL_HOUR, TimeShout.normalizeInterval(7))
    }
}
