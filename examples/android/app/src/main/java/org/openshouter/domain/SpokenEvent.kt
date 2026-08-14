package org.openshouter.domain

data class SpokenEvent(
    val kind: Kind,
    val utterance: String,
    val looping: Boolean = false,
) {
    enum class Kind { NOTIFICATION, CALL, POWER, GEO, TIME }
}
