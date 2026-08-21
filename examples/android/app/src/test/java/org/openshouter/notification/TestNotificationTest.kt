package org.openshouter.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TestNotificationTest {
    @Test
    fun onlyOwnTestChannelBypassesAllowlist() {
        assertTrue(TestNotification.isSelfTest("org.openshouter", TestNotification.CHANNEL_ID))
        assertFalse(TestNotification.isSelfTest("org.openshouter", "other"))
        assertFalse(TestNotification.isSelfTest("com.other.app", TestNotification.CHANNEL_ID))
    }
}
