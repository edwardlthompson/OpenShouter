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
    ): Boolean {
        if (!settings.announcerEnabled) return false
        if (insideSilentGeofence) return false
        if (settings.screenOffOnly && screenOn) return false
        if (settings.headsetOnly && !headsetConnected) return false
        if (!device.allows(screenOn, headsetConnected, silentOrVibrate, inCall)) {
            return false
        }
        if (settings.quietHoursEnabled &&
            dayOfWeek in settings.quietDays &&
            inQuietWindow(minuteOfDay, settings.quietStartMinutes, settings.quietEndMinutes)
        ) {
            return false
        }
        return true
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
