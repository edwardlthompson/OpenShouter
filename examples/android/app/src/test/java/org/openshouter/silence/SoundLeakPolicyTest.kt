package org.openshouter.silence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundLeakPolicyTest {
    @Test
    fun blankAndOwnPackageAreSkipped() {
        assertNull(SoundLeakPolicy.evidence(base(packageName = "")))
        assertNull(SoundLeakPolicy.evidence(base(packageName = SoundLeakPolicy.OUR_PACKAGE)))
        assertTrue(SoundLeakPolicy.skip(base(isGroup = true)))
        assertTrue(SoundLeakPolicy.skip(base(silentFlag = true)))
        assertTrue(SoundLeakPolicy.skip(base(importance = 2)))
    }

    @Test
    fun silentPackAndEmptyAreNotLeaks() {
        assertNull(SoundLeakPolicy.evidence(base(channelSound = "")))
        assertNull(SoundLeakPolicy.evidence(base(channelSound = "content://media/1/OpenShouter Silent.wav")))
        assertNull(SoundLeakPolicy.evidence(base(channelSound = null, notificationSound = null)))
    }

    @Test
    fun silentOrNoneClearsALeak() {
        assertEquals(SoundLeakAction.CLEAR, SoundLeakPolicy.action(base(channelSound = "")))
        assertEquals(
            SoundLeakAction.CLEAR,
            SoundLeakPolicy.action(base(channelSound = "content://media/1/OpenShouter Silent.wav")),
        )
        assertEquals(SoundLeakAction.CLEAR, SoundLeakPolicy.action(base(silentFlag = true)))
        assertEquals(SoundLeakAction.CLEAR, SoundLeakPolicy.action(base(importance = 2)))
        assertEquals(SoundLeakAction.IGNORE, SoundLeakPolicy.action(base(isGroup = true)))
        assertEquals(SoundLeakAction.IGNORE, SoundLeakPolicy.action(base(packageName = SoundLeakPolicy.OUR_PACKAGE)))
        assertTrue(SoundLeakPolicy.isSilenced(base(channelSound = null)))
        assertFalse(SoundLeakPolicy.isSilenced(base(channelSound = "content://media/external/audio/media/9")))
    }

    @Test
    fun channelAndNotificationSoundsAreLeaks() {
        assertEquals(
            SoundEvidence.CHANNEL_SOUND,
            SoundLeakPolicy.evidence(base(channelSound = "content://media/external/audio/media/9")),
        )
        assertEquals(
            SoundEvidence.NOTIFICATION_SOUND,
            SoundLeakPolicy.evidence(base(notificationSound = "content://media/external/audio/media/9")),
        )
        assertEquals(
            SoundEvidence.DEFAULT_SOUND,
            SoundLeakPolicy.evidence(base(usesDefaultSound = true)),
        )
        assertEquals(
            SoundEvidence.DEFAULT_SOUND,
            SoundLeakPolicy.evidence(
                base(channelSound = "content://settings/system/notification_sound"),
            ),
        )
        assertEquals(
            SoundLeakAction.UPSERT,
            SoundLeakPolicy.action(base(channelSound = "content://media/external/audio/media/9")),
        )
    }

    @Test
    fun silentSystemDefaultIgnoresDefaultSoundChannels() {
        val def = base(
            channelSound = "content://settings/system/notification_sound",
            defaultNotificationSilent = true,
        )
        assertNull(SoundLeakPolicy.evidence(def))
        assertEquals(SoundLeakAction.CLEAR, SoundLeakPolicy.action(def))
        assertEquals(
            SoundLeakAction.CLEAR,
            SoundLeakPolicy.action(base(usesDefaultSound = true, defaultNotificationSilent = true)),
        )
        assertEquals(
            SoundEvidence.CHANNEL_SOUND,
            SoundLeakPolicy.evidence(
                base(
                    channelSound = "content://media/external/audio/media/9",
                    defaultNotificationSilent = true,
                ),
            ),
        )
    }

    private fun base(
        packageName: String = "sms.app",
        channelSound: String? = null,
        notificationSound: String? = null,
        usesDefaultSound: Boolean = false,
        importance: Int = 3,
        silentFlag: Boolean = false,
        isGroup: Boolean = false,
        defaultNotificationSilent: Boolean = false,
    ) = SoundInspect(
        packageName = packageName,
        channelId = "msg",
        channelName = "Messages",
        channelSound = channelSound,
        notificationSound = notificationSound,
        usesDefaultSound = usesDefaultSound,
        importance = importance,
        silentFlag = silentFlag,
        isGroup = isGroup,
        defaultNotificationSilent = defaultNotificationSilent,
    )
}
