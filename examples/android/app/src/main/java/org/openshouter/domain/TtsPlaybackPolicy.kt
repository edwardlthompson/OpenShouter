package org.openshouter.domain

enum class TtsStream { NOTIFICATION, MEDIA, ALARM }

fun TtsStream.playable(
    thisMuted: Boolean,
    mediaMuted: Boolean,
    ringerSilent: Boolean = false,
    allowSilentVibrate: Boolean = false,
): TtsStream {
    if (ringerSilent && !allowSilentVibrate) return this
    if (!thisMuted) return this
    if (!mediaMuted) return TtsStream.MEDIA
    return this
}

/** Android Auto routes notification-class speech poorly; use media on the car path. */
fun TtsStream.forCarPlayback(carMode: Boolean): TtsStream =
    if (carMode && this == TtsStream.NOTIFICATION) TtsStream.MEDIA else this

/** Output types that should remap to media and publish a playable MediaSession. */
object CarAudioRoute {
    const val TYPE_A2DP = 8
    const val TYPE_USB_ACCESSORY = 12
    const val TYPE_BUS = 21

    fun isCarOutput(type: Int): Boolean = when (type) {
        TYPE_A2DP, TYPE_USB_ACCESSORY, TYPE_BUS -> true
        else -> false
    }
}

data class TtsPlaybackPolicy(
    val stream: TtsStream = TtsStream.MEDIA,
    val delaySeconds: Int = 0,
    val maxLength: Int = 0,
    val audioFocus: Boolean = true,
    val speakEmojis: Boolean = true,
    val repeatMinutes: Int = 0,
    val repeatCount: Int = 0,
    val pauseMedia: Boolean = false,
    val voice: TtsVoice = TtsVoice(),
) {
    fun clamp(): TtsPlaybackPolicy = copy(
        delaySeconds = delaySeconds.coerceIn(0, MAX_DELAY),
        maxLength = maxLength.coerceIn(0, MAX_CHARS),
        repeatMinutes = repeatMinutes.coerceIn(0, MAX_REPEAT),
        repeatCount = repeatCount.coerceIn(0, MAX_REPEAT_COUNT),
        voice = voice.clamp(),
    )

    fun prepareUtterance(text: String): String {
        val stripped = if (speakEmojis) text else stripEmojis(text)
        val collapsed = WHITESPACE.replace(stripped, " ").trim()
        if (collapsed.isEmpty()) return ""
        return if (maxLength > 0) collapsed.take(maxLength).trim() else collapsed
    }

    companion object {
        const val MAX_DELAY = 30
        const val MAX_CHARS = 500
        const val MAX_REPEAT = 60
        const val MAX_REPEAT_COUNT = 3
        private val WHITESPACE = Regex("\\s+")

        private fun stripEmojis(text: String): String {
            val out = StringBuilder(text.length)
            var i = 0
            while (i < text.length) {
                val cp = text.codePointAt(i)
                if (!isEmoji(cp)) out.appendCodePoint(cp)
                i += Character.charCount(cp)
            }
            return out.toString()
        }

        private fun isEmoji(cp: Int): Boolean =
            cp in 0x1F300..0x1FAFF || cp in 0x2600..0x27BF || cp in 0xFE00..0xFE0F
    }
}
