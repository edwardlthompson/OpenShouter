package org.openshouter.tts

import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsPlaybackPolicy
import org.openshouter.domain.TtsStream

internal class TtsPlayback(
    context: Context,
    private val audio: AudioManager,
    private val power: PowerManager,
    private val settings: SettingsRepository,
    private val scope: CoroutineScope,
    private val cacheDir: File,
    private val abandonFocus: () -> Unit,
    private val requestFocus: (TtsPlaybackPolicy, TtsStream, Boolean) -> Unit,
    private val isSilent: () -> Boolean,
    private val carMode: () -> Boolean,
) {
    @Volatile var looping: SpokenEvent? = null
    private val player = TtsFilePlayer(context)
    @Volatile private var lastPolicy = TtsPlaybackPolicy()
    @Volatile private var lastStream = TtsStream.MEDIA
    @Volatile private var lastAllowSilent = false
    @Volatile private var currentUtterance: String? = null
    @Volatile private var currentFile: File? = null
    @Volatile private var playGeneration = 0L
    @Volatile private var generationCurrent: (Long) -> Boolean = { true }
    private var screenOffJob: Job? = null

    fun speakNow(
        engine: TextToSpeech?,
        ready: Boolean,
        event: SpokenEvent,
        policy: TtsPlaybackPolicy,
        allowSilent: Boolean,
        userRequested: Boolean,
        generation: Long,
        isCurrent: (Long) -> Boolean,
        setPending: (SpokenEvent) -> Unit,
    ): Boolean {
        if (!isCurrent(generation)) return false
        val car = carMode()
        if (!car && isSilent() && !allowSilent && !userRequested) return false
        val text = policy.prepareUtterance(event.utterance)
        if (text.isBlank()) return false
        if (engine == null || !ready) {
            setPending(event)
            return false
        }
        lastPolicy = policy
        lastAllowSilent = allowSilent
        playGeneration = generation
        generationCurrent = isCurrent
        looping = event.takeIf { it.looping }
        lastStream = TtsEngine.resolveStream(audio, event.stream ?: policy.stream, allowSilent, car)
        TtsEngine.applyVoice(engine, policy.voice)
        TtsEngine.applyStream(engine, lastStream, car)
        player.stop()
        player.arm(car)
        requestFocus(policy, lastStream, car)
        val id = UUID.randomUUID().toString()
        val file = File(cacheDir, "os-tts-$id.wav")
        currentUtterance = id
        currentFile = file
        TtsEngine.synthesizeToFile(engine, text, file, id)
        return true
    }

    fun onSynthFailed(utteranceId: String?) {
        if (utteranceId != null && utteranceId != currentUtterance) return
        currentFile?.let { runCatching { it.delete() } }
        currentFile = null
        currentUtterance = null
        abandonFocus()
    }

    fun playSynthesized(engine: TextToSpeech?, ready: Boolean, utteranceId: String?) {
        if (utteranceId != null && utteranceId != currentUtterance) return
        val file = currentFile ?: return
        val gen = playGeneration
        val times = 1 + TtsRepeat.extraCount(
            looping ?: SpokenEvent(SpokenEvent.Kind.NOTIFICATION, ""),
            lastPolicy,
        )
        player.play(file, lastStream, times) {
            runCatching { file.delete() }
            if (currentFile == file) currentFile = null
            val again = looping
            if (again == null || !generationCurrent(gen)) {
                if (again == null) abandonFocus()
            } else {
                speakNow(engine, ready, again, lastPolicy, lastAllowSilent, false, gen, generationCurrent) {}
            }
        }
    }

    fun scheduleScreenOff(
        event: SpokenEvent,
        policy: TtsPlaybackPolicy,
        engine: () -> TextToSpeech?,
        ready: () -> Boolean,
        generation: Long,
        isCurrent: (Long) -> Boolean,
        setPending: (SpokenEvent) -> Unit,
    ) {
        TtsScreenOffLoop.start(
            scope, power, settings, event, policy, engine, ready, generation, isCurrent,
            speakNow = { e, r, ev, p, a, u, g, cur, pending ->
                speakNow(e, r, ev, p, a, u, g, cur, pending)
            },
            setPending = setPending,
            cancelPrevious = { cancelScreenOff() },
            assignJob = { screenOffJob = it },
        )
    }

    fun stop() {
        looping = null
        cancelScreenOff()
        player.stop()
        currentFile?.let { runCatching { it.delete() } }
        currentFile = null
        currentUtterance = null
    }

    fun release() {
        stop()
        player.release()
    }

    private fun cancelScreenOff() {
        screenOffJob?.cancel()
        screenOffJob = null
    }
}
