package org.openshouter.domain

/** Package and channel id used to open system notification settings from history. */
object HistoryChannelTarget {
    fun packageOrNull(packageName: String): String? =
        packageName.trim().takeIf { it.isNotEmpty() }

    fun highlightKey(channelId: String): String? =
        channelId.trim().ifEmpty { null }
}
