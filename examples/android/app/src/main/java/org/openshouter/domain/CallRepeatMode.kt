package org.openshouter.domain

enum class CallRepeatMode { OFF, ONCE, UNTIL_ANSWERED }

object CallRepeatModes {
    const val CELLULAR_PACKAGE = "com.android.phone"

    fun parse(stored: Set<String>): Map<String, CallRepeatMode> = stored.mapNotNull { row ->
        val idx = row.indexOf('=')
        if (idx <= 0) return@mapNotNull null
        val pkg = row.substring(0, idx).trim()
        if (pkg.isEmpty()) return@mapNotNull null
        val raw = row.substring(idx + 1).trim()
        val mode = runCatching { CallRepeatMode.valueOf(raw) }.getOrNull()
            ?: CallRepeatMode.ONCE
        pkg to mode
    }.toMap()

    fun encode(map: Map<String, CallRepeatMode>): Set<String> =
        map.mapNotNull { (pkg, mode) ->
            val p = pkg.trim()
            if (p.isEmpty()) null else "$p=${mode.name}"
        }.toSet()

    fun modeFor(packageName: String, map: Map<String, CallRepeatMode>): CallRepeatMode {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return CallRepeatMode.ONCE
        return map[pkg] ?: CallRepeatMode.ONCE
    }

    fun shouldSpeak(mode: CallRepeatMode): Boolean = mode != CallRepeatMode.OFF

    fun looping(mode: CallRepeatMode): Boolean = mode == CallRepeatMode.UNTIL_ANSWERED

    fun spokenRepeatCount(mode: CallRepeatMode, channelRepeat: Int): Int =
        if (mode == CallRepeatMode.ONCE) 0 else channelRepeat
}
