package org.openshouter.tts

/** Pure bind/retry decisions for TextToSpeech engine init (no Android types). */
enum class TtsBindPhase {
    Ready,
    RetryOnce,
    FailedVisible,
}

object TtsBindPolicy {
    fun afterInit(success: Boolean, alreadyRetried: Boolean): TtsBindPhase = when {
        success -> TtsBindPhase.Ready
        !alreadyRetried -> TtsBindPhase.RetryOnce
        else -> TtsBindPhase.FailedVisible
    }

    fun afterThrowable(alreadyRetried: Boolean): TtsBindPhase =
        if (!alreadyRetried) TtsBindPhase.RetryOnce else TtsBindPhase.FailedVisible
}
