package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementGateTest {
    @Test
    fun masterOffBlocks() {
        assertEquals(
            IgnoreReason.GATE_MASTER,
            AnnouncementGate.denyReason(
                AppSettings(announcerEnabled = false),
                12 * 60,
                2,
                screenOn = false,
                headsetConnected = true,
                insideSilentGeofence = false,
            ),
        )
    }

    @Test
    fun silentPlaceIsGatePlace() {
        assertEquals(
            IgnoreReason.GATE_PLACE,
            AnnouncementGate.denyReason(
                AppSettings(),
                12 * 60,
                2,
                screenOn = true,
                headsetConnected = true,
                insideSilentGeofence = true,
            ),
        )
    }

    @Test
    fun quietHoursOvernight() {
        assertTrue(AnnouncementGate.inQuietWindow(23 * 60, 22 * 60, 7 * 60))
        assertTrue(AnnouncementGate.inQuietWindow(3 * 60, 22 * 60, 7 * 60))
        assertFalse(AnnouncementGate.inQuietWindow(12 * 60, 22 * 60, 7 * 60))
    }

    @Test
    fun ringerOrDndIsSilent() {
        assertFalse(RingerSilent.active(ringerNormal = true, dndActive = false))
        assertTrue(RingerSilent.active(ringerNormal = false, dndActive = false))
        assertTrue(RingerSilent.active(ringerNormal = true, dndActive = true))
    }

    @Test
    fun silentOrVibrateHonorsToggle() {
        val off = AppSettings(deviceState = DeviceStatePolicy(allowSilentVibrate = false))
        assertFalse(
            AnnouncementGate.allow(off, 12 * 60, 2, true, true, false, silentOrVibrate = true),
        )
        assertTrue(
            AnnouncementGate.allow(off, 12 * 60, 2, true, true, false, silentOrVibrate = false),
        )
        val on = AppSettings(deviceState = DeviceStatePolicy(allowSilentVibrate = true))
        assertTrue(
            AnnouncementGate.allow(on, 12 * 60, 2, true, true, false, silentOrVibrate = true),
        )
    }

    @Test
    fun headsetOnlyRequiresRoute() {
        val settings = AppSettings(headsetOnly = true)
        assertFalse(
            AnnouncementGate.allow(settings, 12 * 60, 2, false, headsetConnected = false, false),
        )
        assertTrue(
            AnnouncementGate.allow(settings, 12 * 60, 2, false, headsetConnected = true, false),
        )
    }
}
