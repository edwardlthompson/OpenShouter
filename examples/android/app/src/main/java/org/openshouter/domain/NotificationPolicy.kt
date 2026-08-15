package org.openshouter.domain

enum class IgnoreReason {
    NONE,
    EMPTY,
    GROUP,
    REPEAT,
    FILTER,
    GATE,
}

data class NotificationPolicy(
    val ignoreEmpty: Boolean = true,
    val ignoreGroup: Boolean = true,
    val ignoreRepeats: Boolean = true,
    val repeatWindowMs: Long = DEFAULT_REPEAT_WINDOW_MS,
) {
    fun decide(
        title: String,
        text: String,
        isGroupSummary: Boolean,
        key: String,
        lastKey: String?,
        lastAtMillis: Long,
        nowMillis: Long,
    ): IgnoreReason {
        if (ignoreEmpty && title.isBlank() && text.isBlank()) return IgnoreReason.EMPTY
        if (ignoreGroup && isGroupSummary) return IgnoreReason.GROUP
        val withinWindow = nowMillis - lastAtMillis in 0 until repeatWindowMs
        if (ignoreRepeats && key == lastKey && withinWindow) return IgnoreReason.REPEAT
        return IgnoreReason.NONE
    }

    companion object {
        const val DEFAULT_REPEAT_WINDOW_MS = 10_000L

        fun repeatKey(pkg: String, title: String, text: String): String =
            "$pkg\u0000$title\u0000$text"
    }
}
