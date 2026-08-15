package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPolicyTest {
    private val policy = NotificationPolicy()

    @Test
    fun emptyTitleAndTextIgnored() {
        assertEquals(
            IgnoreReason.EMPTY,
            policy.decide("", "", false, "k", null, 0L, 1L),
        )
    }

    @Test
    fun groupSummaryIgnored() {
        assertEquals(
            IgnoreReason.GROUP,
            policy.decide("Ada", "Hi", true, "k", null, 0L, 1L),
        )
    }

    @Test
    fun repeatWithinWindowIgnored() {
        val key = NotificationPolicy.repeatKey("sms", "Ada", "Hi")
        assertEquals(
            IgnoreReason.REPEAT,
            policy.decide("Ada", "Hi", false, key, key, 1_000L, 5_000L),
        )
    }

    @Test
    fun repeatAfterWindowAllowed() {
        val key = NotificationPolicy.repeatKey("sms", "Ada", "Hi")
        assertEquals(
            IgnoreReason.NONE,
            policy.decide("Ada", "Hi", false, key, key, 1_000L, 20_000L),
        )
    }

    @Test
    fun flagsOffAllowEmptyAndGroup() {
        val open = NotificationPolicy(ignoreEmpty = false, ignoreGroup = false, ignoreRepeats = false)
        assertEquals(IgnoreReason.NONE, open.decide("", "", true, "k", "k", 1L, 2L))
    }
}
