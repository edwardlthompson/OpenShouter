package org.openshouter.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarShoutTest {
    @Test
    fun skipsBlankAndRepeatsOnce() {
        assertEquals("", CalendarShout.phrase("  "))
        assertEquals("Standup", CalendarShout.phrase(" Standup "))
        val now = 1_000_000L
        val begin = now + 60_000L
        assertTrue(CalendarShout.shouldSpeak(9, begin, now, null))
        assertFalse(CalendarShout.shouldSpeak(9, begin, now, 9L to begin))
        assertFalse(CalendarShout.shouldSpeak(0, begin, now, null))
        assertFalse(CalendarShout.shouldSpeak(9, now - 1, now, null))
        assertFalse(CalendarShout.shouldSpeak(9, now + CalendarShout.LOOK_AHEAD_MS + 1, now, null))
        assertTrue(CalendarShout.shouldSpeak(9, now + 20 * 60_000L, now, null, CalendarShout.lookAheadMs(30)))
        assertFalse(CalendarShout.shouldSpeak(9, now + 20 * 60_000L, now, null, CalendarShout.lookAheadMs(5)))
    }

    @Test
    fun pickNextSkipsPastAllDayAndHidden() {
        val now = 1_000_000L
        val upcoming = CalendarShout.Event(9, now + 60_000L, "Standup")
        val rows = listOf(
            CalendarShout.Event(1, now - 60_000L, "Already started"),
            CalendarShout.Event(2, now + 30_000L, "All day", allDay = true),
            CalendarShout.Event(3, now + 30_000L, "Hidden", visible = false),
            CalendarShout.Event(4, now + 30_000L, "Declined", declined = true),
            CalendarShout.Event(5, now + 30_000L, "  "),
            upcoming,
        )
        assertEquals(upcoming, CalendarShout.pickNext(rows, now, null))
        assertNull(CalendarShout.pickNext(rows, now, 9L to upcoming.begin))
        assertFalse(CalendarShout.eligible(rows.first(), now, null))
    }
}
