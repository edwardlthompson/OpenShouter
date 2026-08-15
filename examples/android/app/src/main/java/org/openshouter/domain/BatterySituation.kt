package org.openshouter.domain

enum class BatterySituation { LOW, FULL, CONNECTED, DISCONNECTED }

data class BatteryPhrases(
    val enabled: Set<BatterySituation> = BatterySituation.entries.toSet(),
    val low: String = DEFAULT_LOW,
    val full: String = DEFAULT_FULL,
    val connected: String = DEFAULT_CONNECTED,
    val disconnected: String = DEFAULT_DISCONNECTED,
) {
    fun allows(kind: PowerKind): Boolean = situation(kind) in enabled

    fun spoken(event: PowerEvent): String {
        if (!allows(event.kind)) return ""
        val template = when (event.kind) {
            PowerKind.LOW -> low
            PowerKind.FULL -> full
            PowerKind.CONNECTED -> connected
            PowerKind.DISCONNECTED -> disconnected
        }
        return render(template, event.percent)
    }

    companion object {
        const val DEFAULT_LOW = "Battery low%level."
        const val DEFAULT_FULL = "Battery charged%level."
        const val DEFAULT_CONNECTED = "Power connected."
        const val DEFAULT_DISCONNECTED = "Power disconnected."
        const val MAX_PHRASE = 80

        fun situation(kind: PowerKind): BatterySituation = when (kind) {
            PowerKind.LOW -> BatterySituation.LOW
            PowerKind.FULL -> BatterySituation.FULL
            PowerKind.CONNECTED -> BatterySituation.CONNECTED
            PowerKind.DISCONNECTED -> BatterySituation.DISCONNECTED
        }

        fun render(template: String, percent: Int?): String {
            val level = percent?.coerceIn(0, 100)?.let { ", $it percent" }.orEmpty()
            return template.replace("%level", level).trim().take(MAX_PHRASE)
        }
    }
}
