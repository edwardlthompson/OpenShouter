package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import org.openshouter.call.CallChannel

class Sprint15LockTest {
    @Test
    fun simTokenAndNewChannels() {
        val settings = AppSettings(callFormat = "Call from %name on %sim")
        val spoken = CallChannel.incoming(settings, "+15551212", "Pat", "Work")
        assertEquals("Call from Pat on Work", spoken?.utterance)
        assertEquals(ShoutChannel.CALENDAR, ChannelStates.channelFor(SpokenEvent.Kind.CALENDAR))
        assertEquals(ShoutChannel.BLUETOOTH, ChannelStates.channelFor(SpokenEvent.Kind.BLUETOOTH))
        assertEquals(false, AppSettings().calendarShoutEnabled)
        assertEquals(false, AppSettings().bluetoothConnectAlert)
        assertEquals(false, AppSettings().showSpokenText)
    }
}
