package org.openshouter.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.SpokenEvent

class BluetoothShoutTest {
    @Test
    fun skipsBlankNameAndBadBattery() {
        assertNull(BluetoothShout.deviceName("  "))
        assertEquals("Pixel Buds", BluetoothShout.deviceName(" Pixel Buds "))
        assertTrue(BluetoothShout.batteryOk("Buds", 40))
        assertFalse(BluetoothShout.batteryOk("", 40))
        assertFalse(BluetoothShout.batteryOk("Buds", -1))
        assertFalse(BluetoothShout.batteryOk("Buds", 101))
        assertEquals(
            ChannelStates.channelFor(SpokenEvent.Kind.BLUETOOTH),
            org.openshouter.domain.ShoutChannel.BLUETOOTH,
        )
    }
}
