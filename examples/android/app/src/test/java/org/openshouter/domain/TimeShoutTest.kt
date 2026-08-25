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
    fun slotHelpersStayOnTheHour() {
        val onHour = OffsetDateTime.of(2026, 8, 13, 15, 0, 0, 0, utc).toInstant().toEpochMilli()
        val late = OffsetDateTime.of(2026, 8, 13, 15, 1, 30, 0, utc).toInstant().toEpochMilli()
        val tooLate = OffsetDateTime.of(2026, 8, 13, 15, 3, 0, 0, utc).toInstant().toEpochMilli()
        val slot = TimeShout.currentSlotStartMillis(late, TimeShout.INTERVAL_HOUR, utc)
        assertEquals(onHour, slot)
        assertTrue(TimeShout.isSlotAligned(onHour, TimeShout.INTERVAL_HOUR, utc))
        assertFalse(TimeShout.isSlotAligned(late, TimeShout.INTERVAL_HOUR, utc))
        assertTrue(
            TimeShout.shouldSpeakSlot(slot, Long.MIN_VALUE, onHour, requireAligned = true, TimeShout.INTERVAL_HOUR, utc),
        )
        assertFalse(
            TimeShout.shouldSpeakSlot(slot, Long.MIN_VALUE, late, requireAligned = true, TimeShout.INTERVAL_HOUR, utc),
        )
        assertTrue(
            TimeShout.shouldSpeakSlot(slot, Long.MIN_VALUE, late, requireAligned = false, TimeShout.INTERVAL_HOUR, utc),
        )
        assertFalse(
            TimeShout.shouldSpeakSlot(slot, slot, late, requireAligned = false, TimeShout.INTERVAL_HOUR, utc),
        )
        assertFalse(
            TimeShout.shouldSpeakSlot(slot, Long.MIN_VALUE, tooLate, requireAligned = false, TimeShout.INTERVAL_HOUR, utc),
        )
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

    @Test
    fun militarySpeechOnTheHour() {
        assertEquals("twenty-one hundred", MilitaryTime.speak(21, 0))
        assertEquals("zero nine hundred", MilitaryTime.speak(9, 0))
        assertEquals("zero zero hundred", MilitaryTime.speak(0, 0))
        assertEquals("ten hundred", MilitaryTime.speak(10, 0))
        assertEquals("fifteen hundred", MilitaryTime.speak(15, 0))
    }

    @Test
    fun militarySpeechWithMinutes() {
        assertEquals("twenty-one forty-five", MilitaryTime.speak(21, 45))
        assertEquals("zero nine zero five", MilitaryTime.speak(9, 5))
        assertEquals("fifteen thirty", MilitaryTime.speak(15, 30))
        assertEquals("zero zero fifteen", MilitaryTime.speak(0, 15))
        assertEquals("twelve zero five", MilitaryTime.speak(12, 5))
    }

    @Test
    fun formatClockForSpeechUsesMilitaryIn24Hour() {
        val ninePm = OffsetDateTime.of(2026, 8, 15, 21, 0, 0, 0, utc).toZonedDateTime()
        assertEquals(
            "twenty-one hundred",
            TimeShout.formatClockForSpeech(ninePm, TimeHourStyle.HOUR_24, false, Locale.US),
        )
        assertEquals(
            "9:00 PM",
            TimeShout.formatClockForSpeech(ninePm, TimeHourStyle.HOUR_12, true, Locale.US),
        )
    }
}
