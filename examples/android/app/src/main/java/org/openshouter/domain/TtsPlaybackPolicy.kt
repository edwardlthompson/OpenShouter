package org.openshouter.domain

enum class TtsStream { NOTIFICATION, MEDIA, ALARM }

data class TtsPlaybackPolicy(
    val stream: TtsStream = TtsStream.NOTIFICATION,
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
