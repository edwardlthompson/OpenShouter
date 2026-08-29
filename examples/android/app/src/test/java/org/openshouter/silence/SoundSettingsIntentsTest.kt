package org.openshouter.silence

import android.media.RingtoneManager
import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class SoundSettingsIntentsTest {
    @Test
    fun soundSettingsOpensSystemPage() {
        val intent = SoundSettingsIntents.soundSettings()
        assertEquals(Settings.ACTION_SOUND_SETTINGS, intent.action)
        assertTrue(intent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun ringtonePickerShowsSilent() {
        val intent = SoundSettingsIntents.ringtonePicker(RingtoneManager.TYPE_NOTIFICATION)
        assertEquals(RingtoneManager.ACTION_RINGTONE_PICKER, intent.action)
        assertEquals(RingtoneManager.TYPE_NOTIFICATION, intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, 0))
        assertTrue(intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false))
    }

    @Test
    fun writeSettingsTargetsOurPackage() {
        val intent = SoundSettingsIntents.writeSettings("org.openshouter")
        assertEquals(Settings.ACTION_MANAGE_WRITE_SETTINGS, intent.action)
        assertEquals("package:org.openshouter", intent.data.toString())
    }
}
