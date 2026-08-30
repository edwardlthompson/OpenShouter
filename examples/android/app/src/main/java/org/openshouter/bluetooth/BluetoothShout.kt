package org.openshouter.bluetooth

object BluetoothShout {
    const val DEFAULT_LOW_THRESHOLD = 20

    fun deviceName(name: String): String? = name.trim().takeIf { it.isNotEmpty() }

    fun batteryOk(name: String, percent: Int): Boolean =
        deviceName(name) != null && percent in 0..100

    fun batteryThresholdDue(lastPercent: Int?, currentPercent: Int, threshold: Int = DEFAULT_LOW_THRESHOLD): Boolean {
        if (currentPercent !in 0..100) return false
        return (lastPercent == null || lastPercent > threshold) && currentPercent <= threshold
    }

    fun isCarAudioDevice(name: String): Boolean {
        val lower = name.lowercase()
        return lower.contains("car") ||
            lower.contains("auto") ||
            lower.contains("sync") ||
            lower.contains("uconnect") ||
            lower.contains("vehicle") ||
            lower.contains("handsfree")
    }
}

