package org.openshouter.domain

enum class BatterySituation { LOW, FULL, CONNECTED, DISCONNECTED, LEVEL }

data class BatteryPhrases(
    val enabled: Set<BatterySituation> = DEFAULT_ENABLED,
    val low: String = DEFAULT_LOW,
    val full: String = DEFAULT_FULL,
    val connected: String = DEFAULT_CONNECTED,
    val disconnected: String = DEFAULT_DISCONNECTED,
    val level: String = DEFAULT_LEVEL,
) {
    fun allows(kind: PowerKind): Boolean = situation(kind) in enabled

    fun spoken(event: PowerEvent): String {
        if (!allows(event.kind)) return ""
        val template = when (event.kind) {
            PowerKind.LOW -> low
            PowerKind.FULL -> full
            PowerKind.CONNECTED -> connected
            PowerKind.DISCONNECTED -> disconnected
            PowerKind.LEVEL -> level
        }
        return render(template, event.percent, bare = event.kind == PowerKind.LEVEL)
    }

    companion object {
        val DEFAULT_ENABLED = setOf(
            BatterySituation.LOW,
            BatterySituation.FULL,
            BatterySituation.CONNECTED,
            BatterySituation.DISCONNECTED,
        )
        const val DEFAULT_LOW = "Battery low%level."
        const val DEFAULT_FULL = "Battery charged%level."
        const val DEFAULT_CONNECTED = "Power connected."
        const val DEFAULT_DISCONNECTED = "Power disconnected."
        const val DEFAULT_LEVEL = "Battery %level."
        const val MAX_PHRASE = 80

        fun situation(kind: PowerKind): BatterySituation = when (kind) {
            PowerKind.LOW -> BatterySituation.LOW
            PowerKind.FULL -> BatterySituation.FULL
            PowerKind.CONNECTED -> BatterySituation.CONNECTED
            PowerKind.DISCONNECTED -> BatterySituation.DISCONNECTED
            PowerKind.LEVEL -> BatterySituation.LEVEL
        }

        fun render(template: String, percent: Int?, bare: Boolean = false): String {
            val n = percent?.coerceIn(0, 100)
            val level = when {
                n == null -> ""
                bare -> "$n percent"
                else -> ", $n percent"
            }
            return template.replace("%level", level).trim().take(MAX_PHRASE)
        }
    }
}
