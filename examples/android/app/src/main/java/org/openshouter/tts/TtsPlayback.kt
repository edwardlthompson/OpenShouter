package org.openshouter.tts

import android.media.AudioManager
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import java.io.File
import java.util.UUID
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
import org.openshouter.domain.TtsStream

internal class TtsPlayback(
    private val audio: AudioManager,
    private val power: PowerManager,
    private val settings: SettingsRepository,
    private val scope: CoroutineScope,
    cacheDir: File,
    private val abandonFocus: () -> Unit,
    private val requestFocus: (TtsPlaybackPolicy) -> Unit,
) {
    @Volatile var looping: SpokenEvent? = null
    private val player = TtsFilePlayer()
    private val synthFile = File(cacheDir, "os-tts.wav")
    @Volatile private var lastPolicy = TtsPlaybackPolicy()
    @Volatile private var lastStream = TtsStream.MEDIA
    @Volatile private var lastAllowSilent = true
    private var screenOffJob: Job? = null

    fun speakNow(
        engine: TextToSpeech?,
        ready: Boolean,
        event: SpokenEvent,
        policy: TtsPlaybackPolicy,
        allowSilent: Boolean,
        userRequested: Boolean,
        setPending: (SpokenEvent) -> Unit,
    ): Boolean {
        val silent = audio.ringerMode != AudioManager.RINGER_MODE_NORMAL
        if (silent && !allowSilent && !userRequested) return false
        val text = policy.prepareUtterance(event.utterance)
        if (text.isBlank()) return false
        if (engine == null || !ready) {
            setPending(event)
            return false
        }
        lastPolicy = policy
        lastAllowSilent = allowSilent
        looping = event.takeIf { it.looping }
        lastStream = TtsEngine.resolveStream(audio, event.stream ?: policy.stream, allowSilent)
        TtsEngine.applyVoice(engine, policy.voice)
        requestFocus(policy)
        player.stop()
        TtsEngine.synthesizeToFile(engine, text, synthFile, UUID.randomUUID().toString())
        return true
    }

    fun playSynthesized(engine: TextToSpeech?, ready: Boolean) {
        val times = 1 + TtsRepeat.extraCount(
            looping ?: SpokenEvent(SpokenEvent.Kind.NOTIFICATION, ""),
            lastPolicy,
        )
        player.play(synthFile, lastStream, times) {
            val again = looping
            if (again == null) abandonFocus() else {
                speakNow(engine, ready, again, lastPolicy, lastAllowSilent, false) {}
            }
        }
    }

    fun scheduleScreenOff(
        event: SpokenEvent,
        policy: TtsPlaybackPolicy,
        engine: () -> TextToSpeech?,
        ready: () -> Boolean,
        setPending: (SpokenEvent) -> Unit,
    ) {
        cancelScreenOff()
        if (policy.repeatMinutes <= 0) return
        screenOffJob = scope.launch {
            while (isActive) {
                delay(TtsRepeat.delayMs(policy.repeatMinutes))
                if (!TtsRepeat.screenIsOff(power.isInteractive)) break
                withContext(Dispatchers.Main) {
                    val snap = settings.snapshot()
                    speakNow(
                        engine(),
                        ready(),
                        event,
                        snap.ttsPlayback.clamp(),
                        ChannelStates.allowSilentVibrate(snap, event.kind),
                        false,
                        setPending,
                    )
                }
            }
        }
    }

    fun stop() {
        looping = null
        cancelScreenOff()
        player.stop()
    }

    private fun cancelScreenOff() {
        screenOffJob?.cancel()
        screenOffJob = null
    }
}
