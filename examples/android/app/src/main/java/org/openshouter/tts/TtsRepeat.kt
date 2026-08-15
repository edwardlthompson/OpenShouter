package org.openshouter.tts

import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsPlaybackPolicy

internal object TtsRepeat {
    fun extraCount(event: SpokenEvent, policy: TtsPlaybackPolicy): Int {
        val n = if (event.repeatCount > 0) event.repeatCount else policy.repeatCount
        return n.coerceIn(0, TtsPlaybackPolicy.MAX_REPEAT_COUNT)
    }

    fun delayMs(repeatMinutes: Int): Long = repeatMinutes * 60_000L

    fun screenIsOff(interactive: Boolean): Boolean = !interactive
}
