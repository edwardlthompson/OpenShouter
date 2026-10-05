package org.openshouter.tts

import android.os.PowerManager
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsPlaybackPolicy

internal object TtsScreenOffLoop {
    fun start(
        scope: CoroutineScope,
        power: PowerManager,
        settings: SettingsRepository,
        event: SpokenEvent,
        policy: TtsPlaybackPolicy,
        engine: () -> TextToSpeech?,
        ready: () -> Boolean,
        generation: Long,
        isCurrent: (Long) -> Boolean,
        speakNow: (
            TextToSpeech?, Boolean, SpokenEvent, TtsPlaybackPolicy, Boolean, Boolean, Long,
            (Long) -> Boolean, (SpokenEvent) -> Unit,
        ) -> Boolean,
        setPending: (SpokenEvent) -> Unit,
        cancelPrevious: () -> Unit,
        assignJob: (Job?) -> Unit,
    ) {
        cancelPrevious()
        if (policy.repeatMinutes <= 0) {
            assignJob(null)
            return
        }
        assignJob(
            scope.launch {
                while (isActive) {
                    delay(TtsRepeat.delayMs(policy.repeatMinutes))
                    if (!TtsRepeat.screenIsOff(power.isInteractive)) break
                    if (!isCurrent(generation)) break
                    withContext(Dispatchers.Main) {
                        if (!isCurrent(generation)) return@withContext
                        val snap = settings.snapshot()
                        speakNow(
                            engine(), ready(), event, snap.ttsPlayback.clamp(),
                            ChannelStates.allowPlaybackWhenSilent(snap, event.kind),
                            false, generation, isCurrent, setPending,
                        )
                    }
                }
            },
        )
    }
}
