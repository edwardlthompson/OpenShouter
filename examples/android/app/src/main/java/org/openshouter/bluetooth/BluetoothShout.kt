package org.openshouter.bluetooth

object BluetoothShout {
    fun deviceName(name: String): String? = name.trim().takeIf { it.isNotEmpty() }

    fun batteryOk(name: String, percent: Int): Boolean =
        deviceName(name) != null && percent in 0..100
}
