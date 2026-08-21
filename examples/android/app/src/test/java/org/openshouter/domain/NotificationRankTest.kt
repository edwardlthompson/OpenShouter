package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationRankTest {
    @Test
    fun mapsPriorityAndChannel() {
        assertEquals(NotificationRank.DEFAULT, NotificationRank.fromPriority(0))
        assertEquals(NotificationRank.HIGH, NotificationRank.fromPriority(1))
        assertEquals(NotificationRank.HIGH, NotificationRank.effective(-2, 4))
        assertTrue(NotificationRank.allows(SpeakImportance.DEFAULT, NotificationRank.HIGH))
        assertFalse(NotificationRank.allows(SpeakImportance.HIGH, NotificationRank.DEFAULT))
        assertEquals(SpeakImportance.LOW, NotificationRank.parseImportance("LOW"))
        assertEquals(SpeakImportance.ANY, NotificationRank.parseImportance("nope"))
    }
}
