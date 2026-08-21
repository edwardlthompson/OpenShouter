package org.openshouter.domain

object AnnouncementGate {
    fun allow(
        settings: AppSettings,
        minuteOfDay: Int,
        dayOfWeek: Int,
        screenOn: Boolean,
        headsetConnected: Boolean,
        insideSilentGeofence: Boolean,
        silentOrVibrate: Boolean = false,
        inCall: Boolean = false,
        device: DeviceStatePolicy = settings.deviceState,
    ): Boolean = denyReason(
        settings,
        minuteOfDay,
        dayOfWeek,
        screenOn,
        headsetConnected,
        insideSilentGeofence,
        silentOrVibrate,
        inCall,
        device,
    ) == null

    fun denyReason(
        settings: AppSettings,
        minuteOfDay: Int,
        dayOfWeek: Int,
        screenOn: Boolean,
        headsetConnected: Boolean,
        insideSilentGeofence: Boolean,
        silentOrVibrate: Boolean = false,
        inCall: Boolean = false,
        device: DeviceStatePolicy = settings.deviceState,
    ): IgnoreReason? {
        if (!settings.announcerEnabled) return IgnoreReason.GATE_MASTER
        if (insideSilentGeofence) return IgnoreReason.GATE_PLACE
        if (settings.screenOffOnly && screenOn) return IgnoreReason.GATE_SCREEN
        if (settings.headsetOnly && !headsetConnected) return IgnoreReason.GATE_HEADSET
        device.denyReason(screenOn, headsetConnected, silentOrVibrate, inCall)?.let { return it }
        if (settings.quietHoursEnabled &&
            dayOfWeek in settings.quietDays &&
            inQuietWindow(minuteOfDay, settings.quietStartMinutes, settings.quietEndMinutes)
        ) {
            return IgnoreReason.GATE_QUIET
        }
        return null
    }

    fun inQuietWindow(minuteOfDay: Int, start: Int, end: Int): Boolean {
        if (start == end) return false
        return if (start < end) {
            minuteOfDay in start until end
        } else {
            minuteOfDay >= start || minuteOfDay < end
        }
    }
}
