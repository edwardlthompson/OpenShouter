package org.openshouter.tts

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsPlaybackPolicy

@Singleton
class TtsController @Inject constructor(
    @ApplicationContext context: Context,
    private val settings: SettingsRepository,
) {
    private val appContext = context.applicationContext
    private val audio = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val power = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile private var engine: TextToSpeech? = null
    @Volatile private var ready = false
    @Volatile private var looping: SpokenEvent? = null
    @Volatile private var pending: SpokenEvent? = null
    @Volatile private var lastPolicy: TtsPlaybackPolicy = TtsPlaybackPolicy()
    @Volatile private var focusRequest: AudioFocusRequest? = null
    @Volatile private var screenOffJob: Job? = null

    fun languageTags(): List<String> {
        warmup()
        return TtsEngine.languageTags(engine)
    }

    fun warmup() {
        if (engine != null) return
        engine = TextToSpeech(appContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            engine?.language = Locale.getDefault()
            engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onError(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    if (looping == null) abandonFocus()
                    val again = looping ?: return
                    speakNow(again, lastPolicy)
                }
            })
            pending?.let { queued ->
                pending = null
                speak(queued)
            }
        }
    }

    fun speak(event: SpokenEvent) {
        warmup()
        scope.launch {
            val policy = settings.snapshot().ttsPlayback.clamp()
            val skipDelay = event.looping && looping != null
            if (!skipDelay && policy.delaySeconds > 0) {
                delay(policy.delaySeconds * 1000L)
            }
            if (speakNow(event, policy)) scheduleScreenOff(event, policy)
        }
    }

    private fun speakNow(event: SpokenEvent, policy: TtsPlaybackPolicy): Boolean {
        val text = policy.prepareUtterance(event.utterance)
        if (text.isBlank()) return false
        val tts = engine
        if (tts == null || !ready) {
            pending = event
            return false
        }
        pending = null
        lastPolicy = policy
        looping = event.takeIf { it.looping }
        TtsEngine.applyStream(tts, event.stream ?: policy.stream)
        TtsEngine.applyVoice(tts, policy.voice)
        if (policy.audioFocus) focusRequest = TtsEngine.requestFocus(audio, policy.pauseMedia)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        repeat(TtsRepeat.extraCount(event, policy)) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
        }
        return true
    }

    private fun scheduleScreenOff(event: SpokenEvent, policy: TtsPlaybackPolicy) {
        cancelScreenOff()
        if (policy.repeatMinutes <= 0) return
        screenOffJob = scope.launch {
            while (isActive) {
                delay(TtsRepeat.delayMs(policy.repeatMinutes))
                if (!TtsRepeat.screenIsOff(power.isInteractive)) break
                speakNow(event, policy)
            }
        }
    }

    fun interrupt() {
        looping = null
        cancelScreenOff()
        engine?.stop()
        abandonFocus()
    }

    fun shutdown() {
        looping = null
        cancelScreenOff()
        abandonFocus()
        engine?.shutdown()
        engine = null
        ready = false
    }

    private fun cancelScreenOff() {
        screenOffJob?.cancel()
        screenOffJob = null
    }

    private fun abandonFocus() {
        TtsEngine.abandonFocus(audio, focusRequest)
        focusRequest = null
    }
}
