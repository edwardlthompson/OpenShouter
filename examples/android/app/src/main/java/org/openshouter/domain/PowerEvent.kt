package org.openshouter.domain

enum class PowerKind { LOW, FULL, CONNECTED, DISCONNECTED }

data class PowerEvent(val kind: PowerKind, val percent: Int?)

object PowerRules {
    fun spoken(event: PowerEvent): String = when (event.kind) {
        PowerKind.LOW -> "Battery low${event.percent?.let { ", $it percent" }.orEmpty()}."
        PowerKind.FULL -> "Battery charged${event.percent?.let { ", $it percent" }.orEmpty()}."
        PowerKind.CONNECTED -> "Power connected."
        PowerKind.DISCONNECTED -> "Power disconnected."
    }

    fun isFullThreshold(percent: Int, settings: AppSettings): Boolean =
        percent >= settings.batteryFullPercent

    fun isLowThreshold(percent: Int, settings: AppSettings): Boolean =
        percent <= settings.batteryLowPercent
}
