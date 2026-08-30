package org.openshouter.geo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.bluetooth.BluetoothShout
import org.openshouter.domain.QuietHours
import org.openshouter.domain.QuietProfile
import org.openshouter.gesture.FlipMode
import org.openshouter.gesture.FlipSensitivity
import org.openshouter.places.FossMapPicker

class PlacesAndStatesSprint30Test {

    @Test
    fun fossMapPickerCalculations() {
        assertEquals(50f, FossMapPicker.clampRadius(10f))
        assertEquals(500f, FossMapPicker.clampRadius(500f))
        assertEquals(2000f, FossMapPicker.clampRadius(5000f))

        val tileX = FossMapPicker.lonToTileX(-122.4194, 10)
        assertTrue(tileX in 0 until (1 shl 10))

        val tileY = FossMapPicker.latToTileY(37.7749, 10)
        assertTrue(tileY in 0 until (1 shl 10))

        val tileUrl = FossMapPicker.osmTileUrl(tileX, tileY, 10)
        assertTrue(tileUrl.startsWith("https://tile.openstreetmap.org/10/"))
    }

    @Test
    fun quietHoursProfiles() {
        val home = QuietHours.profileFor(QuietProfile.HOME)
        assertEquals(22 * 60, home.startMinutes)
        assertEquals(7 * 60, home.endMinutes)
        assertEquals(7, home.days.size)

        val work = QuietHours.profileFor(QuietProfile.WORK)
        assertEquals(9 * 60, work.startMinutes)
        assertEquals(17 * 60, work.endMinutes)
        assertEquals(setOf(2, 3, 4, 5, 6), work.days)

        val weekend = QuietHours.profileFor(QuietProfile.WEEKEND)
        assertEquals(23 * 60, weekend.startMinutes)
        assertEquals(9 * 60, weekend.endMinutes)
        assertEquals(setOf(1, 7), weekend.days)
    }

    @Test
    fun carBluetoothDetection() {
        assertTrue(BluetoothShout.isCarAudioDevice("My Car BT"))
        assertTrue(BluetoothShout.isCarAudioDevice("Ford SYNC"))
        assertTrue(BluetoothShout.isCarAudioDevice("UConnect Handsfree"))
        assertTrue(BluetoothShout.isCarAudioDevice("Automotive Audio"))
        assertFalse(BluetoothShout.isCarAudioDevice("Sony WH-1000XM4"))
        assertFalse(BluetoothShout.isCarAudioDevice("Pixel Buds"))
    }

    @Test
    fun flipSensitivityDeskVsPocket() {
        // Flat on desk: Z is strongly downward (-9.8), X and Y close to 0
        assertTrue(FlipSensitivity.isFaceDown(0.1f, 0.2f, -9.8f, FlipMode.DESK_STRICT))
        // Tilted (e.g. in pocket): Z is -8.0, X is 3.5 -> passes pocket mode, fails desk mode
        assertFalse(FlipSensitivity.isFaceDown(3.5f, 1.0f, -8.0f, FlipMode.DESK_STRICT))
        assertTrue(FlipSensitivity.isFaceDown(3.5f, 1.0f, -8.0f, FlipMode.POCKET_TOLERANT))
    }
}
