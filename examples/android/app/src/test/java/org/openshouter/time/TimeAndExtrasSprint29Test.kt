package org.openshouter.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.bluetooth.BluetoothShout
import org.openshouter.calendar.CalendarShout
import org.openshouter.domain.TimeShout

class TimeAndExtrasSprint29Test {

    @Test
    fun customTimeShoutIntervals() {
        assertEquals(5, TimeShout.normalizeInterval(5))
        assertEquals(10, TimeShout.normalizeInterval(10))
        assertEquals(20, TimeShout.normalizeInterval(20))
        assertEquals(45, TimeShout.normalizeInterval(45))
        assertEquals(120, TimeShout.normalizeInterval(120))
        assertEquals(60, TimeShout.normalizeInterval(0))
        assertEquals(60, TimeShout.normalizeInterval(-10))
        assertEquals(60, TimeShout.normalizeInterval(999))
    }

    @Test
    fun calendarAllowlistFiltering() {
        val workEvent = CalendarShout.Event(
            eventId = 1L,
            begin = 1000L,
            title = "Team Standup",
            calendarName = "Work",
        )
        val personalEvent = CalendarShout.Event(
            eventId = 2L,
            begin = 2000L,
            title = "Dentist",
            calendarName = "Personal",
        )

        val allowWork = setOf("Work")
        assertTrue(CalendarShout.isAllowed(workEvent, allowWork))
        assertFalse(CalendarShout.isAllowed(personalEvent, allowWork))

        val allowEmpty = emptySet<String>()
        assertTrue(CalendarShout.isAllowed(workEvent, allowEmpty))
        assertTrue(CalendarShout.isAllowed(personalEvent, allowEmpty))
    }

    @Test
    fun allDayMorningBriefing() {
        val allDay1 = CalendarShout.Event(
            eventId = 10L,
            begin = 0L,
            title = "Birthday",
            allDay = true,
        )
        val allDay2 = CalendarShout.Event(
            eventId = 11L,
            begin = 0L,
            title = "Holiday",
            allDay = true,
        )
        val regular = CalendarShout.Event(
            eventId = 12L,
            begin = 1000L,
            title = "Meeting",
            allDay = false,
        )

        val singleBriefing = CalendarShout.morningBriefing(listOf(allDay1, regular))
        assertEquals("Today's all-day event: Birthday", singleBriefing)

        val multiBriefing = CalendarShout.morningBriefing(listOf(allDay1, allDay2))
        assertEquals("Today's all-day events: Birthday, Holiday", multiBriefing)

        val noneBriefing = CalendarShout.morningBriefing(listOf(regular))
        assertNull(noneBriefing)
    }

    @Test
    fun bluetoothBatteryThreshold() {
        assertTrue(BluetoothShout.batteryThresholdDue(lastPercent = null, currentPercent = 15, threshold = 20))
        assertTrue(BluetoothShout.batteryThresholdDue(lastPercent = 25, currentPercent = 20, threshold = 20))
        assertTrue(BluetoothShout.batteryThresholdDue(lastPercent = 80, currentPercent = 15, threshold = 20))
        assertFalse(BluetoothShout.batteryThresholdDue(lastPercent = 15, currentPercent = 10, threshold = 20))
        assertFalse(BluetoothShout.batteryThresholdDue(lastPercent = 50, currentPercent = 30, threshold = 20))
        assertFalse(BluetoothShout.batteryThresholdDue(lastPercent = 50, currentPercent = -1, threshold = 20))
    }
}
