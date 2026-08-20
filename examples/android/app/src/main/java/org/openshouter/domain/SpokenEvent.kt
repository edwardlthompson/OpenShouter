package org.openshouter.domain

data class SpokenEvent(
    val kind: Kind,
    val utterance: String,
    val looping: Boolean = false,
    val repeatCount: Int = 0,
    val stream: TtsStream? = null,
) {
    enum class Kind { NOTIFICATION, CALL, POWER, GEO, TIME, MESSAGE, REMINDER, CALENDAR, BLUETOOTH }
}
