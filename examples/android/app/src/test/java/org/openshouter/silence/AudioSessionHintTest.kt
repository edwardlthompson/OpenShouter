package org.openshouter.silence

import android.media.AudioAttributes
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class AudioSessionHintTest {
    @Test
    fun skipsOurUidAndMediaPlayback() {
        assertFalse(AudioSessionHint.shouldRecord(1000, 1000, AudioAttributes.USAGE_NOTIFICATION))
        assertFalse(AudioSessionHint.shouldRecord(-1, 1000, AudioAttributes.USAGE_NOTIFICATION))
        assertFalse(AudioSessionHint.shouldRecord(2000, 1000, AudioAttributes.USAGE_MEDIA))
        assertTrue(AudioSessionHint.shouldRecord(2000, 1000, AudioAttributes.USAGE_NOTIFICATION))
        assertTrue(AudioSessionHint.shouldRecord(2000, 1000, AudioAttributes.USAGE_NOTIFICATION_RINGTONE))
    }

    @Test
    fun skipsSystemAndOurPackage() {
        assertTrue(AudioSessionHint.skipPackage("org.openshouter", "org.openshouter"))
        assertTrue(AudioSessionHint.skipPackage("android", "org.openshouter"))
        assertTrue(AudioSessionHint.skipPackage("com.android.systemui", "org.openshouter"))
        assertFalse(AudioSessionHint.skipPackage("sms.app", "org.openshouter"))
    }
}
