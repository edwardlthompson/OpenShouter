package org.openshouter.domain

data class DeviceStatePolicy(
    val allowScreenOn: Boolean = true,
    val allowScreenOff: Boolean = true,
    val allowHeadsetOn: Boolean = true,
    val allowHeadsetOff: Boolean = true,
    val allowSilentVibrate: Boolean = false,
    val allowInCall: Boolean = false,
) {
    fun allows(
        screenOn: Boolean,
        headsetOn: Boolean,
        silentOrVibrate: Boolean,
        inCall: Boolean,
    ): Boolean {
        if (screenOn && !allowScreenOn) return false
        if (!screenOn && !allowScreenOff) return false
        if (headsetOn && !allowHeadsetOn) return false
        if (!headsetOn && !allowHeadsetOff) return false
        if (silentOrVibrate && !allowSilentVibrate) return false
        if (inCall && !allowInCall) return false
        return true
    }

    fun denyReason(
        screenOn: Boolean,
        headsetOn: Boolean,
        silentOrVibrate: Boolean,
        inCall: Boolean,
    ): IgnoreReason? {
        if (screenOn && !allowScreenOn) return IgnoreReason.GATE_SCREEN
        if (!screenOn && !allowScreenOff) return IgnoreReason.GATE_SCREEN
        if (headsetOn && !allowHeadsetOn) return IgnoreReason.GATE_HEADSET
        if (!headsetOn && !allowHeadsetOff) return IgnoreReason.GATE_HEADSET
        if (silentOrVibrate && !allowSilentVibrate) return IgnoreReason.GATE_SILENT
        if (inCall && !allowInCall) return IgnoreReason.GATE_CALL
        return null
    }
}
