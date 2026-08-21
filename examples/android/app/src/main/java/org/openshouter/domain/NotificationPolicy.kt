package org.openshouter.domain

enum class IgnoreReason {
    NONE,
    EMPTY,
    GROUP,
    REPEAT,
    FILTER,
    GATE,
    GATE_MASTER,
    GATE_PLACE,
    GATE_QUIET,
    GATE_SCREEN,
    GATE_HEADSET,
    GATE_SILENT,
    GATE_CALL,
    IMPORTANCE,
}

data class NotificationPolicy(
    val ignoreEmpty: Boolean = true,
    val ignoreGroup: Boolean = true,
    val ignoreRepeats: Boolean = true,
    val collapseRepeats: Boolean = true,
    val repeatWindowMs: Long = DEFAULT_REPEAT_WINDOW_MS,
    val collapseWindowMs: Long = DEFAULT_COLLAPSE_WINDOW_MS,
    val minImportance: SpeakImportance = SpeakImportance.ANY,
    val dndPriorityOnly: Boolean = true,
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

    fun recordIgnore(
        reason: IgnoreReason,
        key: String,
        lastRecordedKey: String?,
        lastRecordedAt: Long,
        nowMillis: Long,
    ): Boolean {
        if (reason != IgnoreReason.REPEAT || !collapseRepeats) return true
        val sameBurst = key == lastRecordedKey &&
            nowMillis - lastRecordedAt in 0 until collapseWindowMs
        return !sameBurst
    }

    fun dndExempt(priorityDnd: Boolean, highOrCall: Boolean): Boolean =
        dndPriorityOnly && priorityDnd && highOrCall

    companion object {
        const val DEFAULT_REPEAT_WINDOW_MS = 10_000L
        const val DEFAULT_COLLAPSE_WINDOW_MS = 60_000L

        fun repeatKey(pkg: String, title: String, text: String): String =
            "$pkg\u0000$title\u0000$text"
    }
}
