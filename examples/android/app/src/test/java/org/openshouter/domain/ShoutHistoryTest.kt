package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.data.ShoutHistoryStore

class ShoutHistoryTest {
    @Test
    fun recordsBuiltInShoutsButNotAppNotifications() {
        assertTrue(ShoutHistory.records(SpokenEvent.Kind.TIME))
        assertTrue(ShoutHistory.records(SpokenEvent.Kind.POWER))
        assertTrue(ShoutHistory.records(SpokenEvent.Kind.REMINDER))
        assertTrue(ShoutHistory.records(SpokenEvent.Kind.CALENDAR))
        assertTrue(ShoutHistory.records(SpokenEvent.Kind.BLUETOOTH))
        assertFalse(ShoutHistory.records(SpokenEvent.Kind.NOTIFICATION))
        assertFalse(ShoutHistory.records(SpokenEvent.Kind.MESSAGE))
        assertFalse(ShoutHistory.records(SpokenEvent.Kind.CALL))
    }

    @Test
    fun timeRowStoresKindAndSpokenWithoutTitle() {
        val row = ShoutHistoryStore.row(SpokenEvent.Kind.TIME, "It's 3 o'clock", 9L)!!
        assertEquals(ShoutHistory.PACKAGE, row.packageName)
        assertEquals("", row.title)
        assertEquals("", row.text)
        assertEquals("TIME", row.kind)
        assertEquals("It's 3 o'clock", row.spoken)
        assertEquals(9L, row.postedAt)
    }

    @Test
    fun blankOrNotificationUtteranceIsSkipped() {
        assertNull(ShoutHistoryStore.row(SpokenEvent.Kind.TIME, "  ", 1L))
        assertNull(ShoutHistoryStore.row(SpokenEvent.Kind.NOTIFICATION, "Hello", 1L))
        assertNull(ShoutHistoryStore.row(SpokenEvent.Kind.CALL, "Incoming call", 1L))
    }

    @Test
    fun internalKindDetectsTimeAndIgnoresApps() {
        assertTrue(ShoutHistory.isInternalKind("TIME"))
        assertTrue(ShoutHistory.isInternalKind("POWER"))
        assertFalse(ShoutHistory.isInternalKind("NOTIFICATION"))
        assertFalse(ShoutHistory.isInternalKind("CALL"))
        assertFalse(ShoutHistory.isInternalKind(""))
        assertFalse(ShoutHistory.isInternalKind("sms.app"))
    }
}
