package org.openshouter.domain

enum class SpokenClockStyle {
    NATURAL,
    NATURAL_AMPM,
    DIGIT,
    MILITARY,
    ;

    companion object {
        fun parse(raw: String?): SpokenClockStyle =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: NATURAL
    }
}
