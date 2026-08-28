package org.openshouter.notification

import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class NotificationChannelSettingsTest {
    @Test
    fun blankPackageYieldsNoIntent() {
        assertNull(NotificationChannelSettings.intent("", "msg"))
        assertNull(NotificationChannelSettings.fallback("  ", "msg"))
    }

    @Test
    fun channelIdOpensChannelPageWithHighlight() {
        val intent = NotificationChannelSettings.intent("sms.app", "msg")!!
        assertEquals(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS, intent.action)
        assertEquals("sms.app", intent.getStringExtra(Settings.EXTRA_APP_PACKAGE))
        assertEquals("msg", intent.getStringExtra(Settings.EXTRA_CHANNEL_ID))
        assertEquals("msg", intent.getStringExtra(NotificationChannelSettings.FRAGMENT_ARG_KEY))
        assertTrue(intent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun missingChannelOpensAppNotificationSettings() {
        val intent = NotificationChannelSettings.intent("sms.app", "")!!
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, intent.action)
        assertEquals("sms.app", intent.getStringExtra(Settings.EXTRA_APP_PACKAGE))
        assertNull(intent.getStringExtra(Settings.EXTRA_CHANNEL_ID))
        assertNull(NotificationChannelSettings.fallback("sms.app", "  "))
    }

    @Test
    fun fallbackFlashesChannelInAppSettings() {
        val intent = NotificationChannelSettings.fallback("sms.app", "msg")!!
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, intent.action)
        assertEquals("msg", intent.getStringExtra(Settings.EXTRA_CHANNEL_ID))
        assertEquals("msg", intent.getStringExtra(NotificationChannelSettings.FRAGMENT_ARG_KEY))
        val args = intent.getBundleExtra(NotificationChannelSettings.SHOW_FRAGMENT_ARGS)
        assertEquals("msg", args?.getString(NotificationChannelSettings.FRAGMENT_ARG_KEY))
    }
}
