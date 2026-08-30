package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.data.ReminderEntity

class ChannelPolicyTest {
    @Test
    fun messageChannelDefaultsOff() {
        assertFalse(MessageChannelPolicy().allows(fromKnownContact = true))
        assertTrue(MessageChannelPolicy(enabled = true).allows(fromKnownContact = false))
        assertFalse(MessageChannelPolicy(enabled = true, speakUnknown = false).allows(false))
        assertFalse(MessageChannelPolicy(enabled = true, knownContactsOnly = true).allows(false))
    }

    @Test
    fun missedCallSkipsUnknownWhenDisabled() {
        assertFalse(MissedCallPolicy().allows(true))
        assertTrue(MissedCallPolicy(enabled = true).allows(false))
        assertFalse(MissedCallPolicy(enabled = true, speakUnknown = false).allows(false))
    }

    @Test
    fun timeScheduleNormalizesInterval() {
        val schedule = TimeShoutSchedule(enabled = true, intervalMinutes = 0, exact = false).normalized()
        assertEquals(TimeShout.INTERVAL_HOUR, schedule.intervalMinutes)
        assertFalse(schedule.exact)
    }

    @Test
    fun alarmPolicyRequiresBothFlags() {
        assertTrue(AlarmPolicy.useExact(wantExact = true, canScheduleExact = true))
        assertFalse(AlarmPolicy.useExact(wantExact = true, canScheduleExact = false))
        assertFalse(AlarmPolicy.useExact(wantExact = false, canScheduleExact = true))
    }

    @Test
    fun appOverrideMergesBlankToGlobal() {
        assertEquals("%app", AppOverride("pkg", null).mergeFormat("%app"))
        assertEquals("%text", AppOverride("pkg", "%text").mergeFormat("%app"))
        assertEquals(mapOf("a" to "%app"), AppOverrides.parse(setOf("a=%app")))
    }

    @Test
    fun reminderTextRejectsBlank() {
        assertNull(ReminderEntity.normalizeText("   "))
        assertEquals("hello", ReminderEntity.normalizeText(" hello "))
        assertEquals(ReminderEntity.MAX_TEXT, ReminderEntity.normalizeText("x".repeat(300))?.length)
    }
}
