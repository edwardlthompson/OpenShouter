package org.openshouter.call

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.domain.AppSettings
import org.openshouter.domain.CallRepeatMode
import org.openshouter.domain.CallRepeatModes
import org.openshouter.domain.ChannelDeviceState
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsPlaybackPolicy

class CallRepeatModeTest {
    @Test
    fun defaultOnceForVoip() {
        assertEquals(CallRepeatMode.ONCE, CallRepeatModes.modeFor("com.whatsapp", emptyMap()))
        assertFalse(CallRepeatModes.looping(CallRepeatMode.ONCE))
        assertTrue(CallRepeatModes.shouldSpeak(CallRepeatMode.ONCE))
        assertEquals(0, CallRepeatModes.spokenRepeatCount(CallRepeatMode.ONCE, 3))
    }

    @Test
    fun untilAnsweredSetsLooping() {
        assertTrue(CallRepeatModes.looping(CallRepeatMode.UNTIL_ANSWERED))
        assertEquals(2, CallRepeatModes.spokenRepeatCount(CallRepeatMode.UNTIL_ANSWERED, 2))
        val event = CallPosted.eventFor(
            AppSettings(callRepeatModes = mapOf("com.whatsapp" to CallRepeatMode.UNTIL_ANSWERED)),
            "Ada",
            "",
            "WhatsApp",
            "com.whatsapp",
        )
        assertTrue(event!!.looping)
    }

    @Test
    fun offDoesNotRoute() {
        assertFalse(CallRepeatModes.shouldSpeak(CallRepeatMode.OFF))
        val event = CallPosted.eventFor(
            AppSettings(callRepeatModes = mapOf("com.whatsapp" to CallRepeatMode.OFF)),
            "Ada",
            "",
            "WhatsApp",
            "com.whatsapp",
        )
        assertNull(event)
    }

    @Test
    fun onceForcesRepeatCountZero() {
        val settings = AppSettings(
            channelStates = mapOf(ShoutChannel.CALL to ChannelDeviceState(repeatCount = 3)),
            ttsPlayback = TtsPlaybackPolicy(repeatCount = 3),
        )
        val event = CallPosted.eventFor(settings, "Ada", "", "WhatsApp", "com.whatsapp")
        assertEquals(SpokenEvent.Kind.CALL, event?.kind)
        assertFalse(event!!.looping)
        assertEquals(0, event.repeatCount)
    }

    @Test
    fun parseSkipsBlankAndUnknownBecomesOnce() {
        val parsed = CallRepeatModes.parse(
            setOf("=ONCE", "com.whatsapp=ONCE", "org.signal=NOPE", "  =UNTIL_ANSWERED"),
        )
        assertEquals(CallRepeatMode.ONCE, parsed["com.whatsapp"])
        assertEquals(CallRepeatMode.ONCE, parsed["org.signal"])
        assertFalse(parsed.containsKey(""))
        val encoded = CallRepeatModes.encode(mapOf("" to CallRepeatMode.OFF, "com.whatsapp" to CallRepeatMode.ONCE))
        assertEquals(setOf("com.whatsapp=ONCE"), encoded)
    }
}

class CallLoopGateTest {
    @Test
    fun communicationStopIsVoipOnly() {
        CallLoopGate.clear()
        assertFalse(CallLoopGate.shouldStopForCommunicationMode(true))
        var cut = 0
        assertFalse(CallLoopGate.cutVoip(true) { cut++ })
        CallLoopGate.onVoipAnnounce("com.whatsapp")
        assertEquals("com.whatsapp", CallLoopGate.activeVoipPackage())
        assertTrue(CallLoopGate.shouldStopForCommunicationMode(true))
        assertFalse(CallLoopGate.shouldStopForCommunicationMode(false))
        assertTrue(CallLoopGate.cutVoip(true) { cut++ })
        assertEquals(1, cut)
        assertNull(CallLoopGate.activeVoipPackage())
        assertFalse(CallLoopGate.shouldStopForCommunicationMode(true))
    }
}
