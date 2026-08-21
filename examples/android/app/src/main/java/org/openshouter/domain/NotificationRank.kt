package org.openshouter.domain

enum class SpeakImportance { ANY, LOW, DEFAULT, HIGH }

object NotificationRank {
    const val MIN = 0
    const val LOW = 1
    const val DEFAULT = 2
    const val HIGH = 3
    const val MAX = 4

    fun fromPriority(priority: Int): Int = (priority + 2).coerceIn(MIN, MAX)

    fun fromChannelImportance(importance: Int): Int = when {
        importance <= 1 -> MIN
        importance == 2 -> LOW
        importance == 3 -> DEFAULT
        importance == 4 -> HIGH
        else -> MAX
    }

    fun effective(priority: Int, channelImportance: Int): Int =
        maxOf(fromPriority(priority), fromChannelImportance(channelImportance))

    fun allows(min: SpeakImportance, rank: Int): Boolean = rank >= minRank(min)

    fun minRank(min: SpeakImportance): Int = when (min) {
        SpeakImportance.ANY -> MIN
        SpeakImportance.LOW -> LOW
        SpeakImportance.DEFAULT -> DEFAULT
        SpeakImportance.HIGH -> HIGH
    }

    fun parseImportance(raw: String?): SpeakImportance =
        runCatching { SpeakImportance.valueOf(raw ?: SpeakImportance.ANY.name) }
            .getOrDefault(SpeakImportance.ANY)
}
