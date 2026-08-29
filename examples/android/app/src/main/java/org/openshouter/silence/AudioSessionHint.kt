package org.openshouter.silence

import android.media.AudioAttributes

object AudioSessionHint {
    const val CHANNEL_OWN = "own-audio"

    fun shouldRecord(clientUid: Int, ourUid: Int, usage: Int): Boolean {
        if (clientUid < 0 || clientUid == ourUid) return false
        return isNotificationUsage(usage)
    }

    fun skipPackage(packageName: String, ourPackage: String): Boolean {
        if (packageName.isBlank() || packageName == ourPackage) return true
        return packageName == "android" || packageName.startsWith("com.android.systemui")
    }

    fun isNotificationUsage(usage: Int): Boolean = usage == AudioAttributes.USAGE_NOTIFICATION ||
        usage == AudioAttributes.USAGE_NOTIFICATION_RINGTONE ||
        usage == AudioAttributes.USAGE_NOTIFICATION_EVENT ||
        usage == AudioAttributes.USAGE_ALARM
}
