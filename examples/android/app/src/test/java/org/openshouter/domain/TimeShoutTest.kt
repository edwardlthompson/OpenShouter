package org.openshouter.domain

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun hourStyleParseAndClock() {
        assertEquals(TimeHourStyle.SYSTEM, TimeHourStyle.parse(null))
        assertEquals(TimeHourStyle.HOUR_12, TimeHourStyle.parse("hour_12"))
        assertEquals(TimeHourStyle.SYSTEM, TimeHourStyle.parse("nope"))
        assertTrue(TimeShout.use24Hour(TimeHourStyle.HOUR_24, false))
        assertFalse(TimeShout.use24Hour(TimeHourStyle.HOUR_12, true))
        assertTrue(TimeShout.use24Hour(TimeHourStyle.SYSTEM, true))
        val fifteen = OffsetDateTime.of(2026, 8, 15, 15, 0, 0, 0, utc).toZonedDateTime()
        assertEquals("3:00 PM", TimeShout.formatClock(fifteen, TimeHourStyle.HOUR_12, true, Locale.US))
        assertEquals("15:00", TimeShout.formatClock(fifteen, TimeHourStyle.HOUR_24, false, Locale.US))
        assertEquals("15:00", TimeShout.formatClock(fifteen, TimeHourStyle.SYSTEM, true, Locale.US))
        val midnight = OffsetDateTime.of(2026, 8, 15, 0, 5, 0, 0, utc).toZonedDateTime()
        assertEquals("12:05 AM", TimeShout.formatClock(midnight, TimeHourStyle.HOUR_12, true, Locale.US))
        assertEquals("00:05", TimeShout.formatClock(midnight, TimeHourStyle.HOUR_24, false, Locale.US))
    }
}
