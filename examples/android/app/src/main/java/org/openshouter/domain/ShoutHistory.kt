package org.openshouter.domain

/** Built-in shouts that are not another app's notification. */
object ShoutHistory {
    const val PACKAGE = "org.openshouter"

    fun records(kind: SpokenEvent.Kind): Boolean = when (kind) {
        SpokenEvent.Kind.NOTIFICATION,
        SpokenEvent.Kind.MESSAGE,
        SpokenEvent.Kind.CALL,
        -> false
        SpokenEvent.Kind.POWER,
        SpokenEvent.Kind.GEO,
        SpokenEvent.Kind.TIME,
        SpokenEvent.Kind.REMINDER,
        SpokenEvent.Kind.CALENDAR,
        SpokenEvent.Kind.BLUETOOTH,
        -> true
    }

    fun isInternalKind(raw: String): Boolean {
        val kind = runCatching { SpokenEvent.Kind.valueOf(raw.trim()) }.getOrNull() ?: return false
        return records(kind)
    }
}
