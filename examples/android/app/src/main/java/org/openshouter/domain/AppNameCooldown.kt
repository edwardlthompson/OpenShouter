package org.openshouter.domain

import kotlin.math.abs

/** How long to wait before saying the same app name again on a shout channel. */
object AppNameCooldown {
    const val DEFAULT_SECONDS = 30
    val OPTIONS_SECONDS = listOf(0, 10, 30, 60, 120, 300)

    fun clampSeconds(raw: Int): Int {
        if (raw <= 0) return 0
        return OPTIONS_SECONDS.minBy { abs(it - raw) }
    }

    fun allow(lastAt: Long, now: Long, seconds: Int): Boolean {
        if (seconds <= 0 || lastAt <= 0L) return true
        return now - lastAt >= seconds * 1000L
    }

    fun secondsFor(settings: AppSettings, channel: ShoutChannel): Int =
        ChannelStates.resolve(
            settings.channelStates,
            channel,
            settings.deviceState,
            settings.ttsPlayback,
        ).appNameCooldownSeconds

    fun include(
        settings: AppSettings,
        channel: ShoutChannel,
        lastAt: Long,
        now: Long,
    ): Boolean = allow(lastAt, now, secondsFor(settings, channel))

    fun isAppLabel(value: String, label: String): Boolean {
        val spoken = value.trim()
        val app = label.trim()
        return spoken.isNotEmpty() && app.isNotEmpty() && spoken.equals(app, ignoreCase = true)
    }
}
