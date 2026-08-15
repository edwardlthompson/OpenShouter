package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuietHoursTest {
    @Test
    fun wrapsMidnightWhenNudging() {
        assertEquals(0, QuietHours.nudge(23 * 60 + 45, 1))
        assertEquals(23 * 60 + 45, QuietHours.nudge(0, -1))
    }

    @Test
    fun labelsDefaultWindow() {
        assertEquals("12:00 AM", QuietHours.clockLabel(0))
        assertEquals("12:00 PM", QuietHours.clockLabel(12 * 60))
        assertEquals("10:00 PM", QuietHours.clockLabel(22 * 60))
        assertEquals("7:00 AM", QuietHours.clockLabel(7 * 60))
        assertEquals("10:00 PM–7:00 AM", QuietHours.windowLabel(22 * 60, 7 * 60))
    }

    @Test
    fun keepsLastDaySelected() {
        val onlySunday = setOf(1)
        assertEquals(onlySunday, QuietHours.toggleDay(onlySunday, 1))
        assertEquals(setOf(1, 2), QuietHours.toggleDay(onlySunday, 2))
        assertFalse(QuietHours.toggleDay(setOf(1, 2), 2).contains(2))
        assertTrue(QuietHours.toggleDay(setOf(1, 2), 2).contains(1))
    }
}
