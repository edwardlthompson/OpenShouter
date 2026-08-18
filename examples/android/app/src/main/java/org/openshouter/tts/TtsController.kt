package org.openshouter.tts

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openshouter.audio.AudioRouteMonitor
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.SpokenEvent

@Singleton
class TtsController @Inject constructor(
    @ApplicationContext context: Context,
    private val settings: SettingsRepository,
    route: AudioRouteMonitor,
) {
    private val appContext = context.applicationContext
    private val audio = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile private var engine: TextToSpeech? = null
    @Volatile private var ready = false
    @Volatile private var pending: SpokenEvent? = null
    @Volatile private var focusRequest: AudioFocusRequest? = null
    private val playback = TtsPlayback(
        audio,
        appContext.getSystemService(Context.POWER_SERVICE) as PowerManager,
        settings,
        scope,
        appContext.cacheDir,
        abandonFocus = { abandonFocus() },
        requestFocus = { policy, stream ->
            if (policy.audioFocus) {
                focusRequest = TtsEngine.requestFocus(audio, policy.pauseMedia, stream)
            }
        },
        isSilent = { route.isSilent() },
    )

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
                override fun onError(utteranceId: String?) {
                    scope.launch(Dispatchers.Main.immediate) { playback.onSynthFailed(utteranceId) }
                }
                override fun onDone(utteranceId: String?) {
                    scope.launch(Dispatchers.Main.immediate) {
                        playback.playSynthesized(engine, ready, utteranceId)
                    }
                }
            })
            pending?.let { queued ->
                pending = null
                speak(queued)
            }
        }
    }

    fun speak(event: SpokenEvent, immediate: Boolean = false) {
        warmup()
        scope.launch {
            val snap = settings.snapshot()
            val policy = snap.ttsPlayback.clamp()
            val allowSilent = ChannelStates.allowSilentVibrate(snap, event.kind)
            if (!immediate && !(event.looping && playback.looping != null) && policy.delaySeconds > 0) {
                delay(policy.delaySeconds * 1000L)
            }
            withContext(Dispatchers.Main) {
                if (playback.speakNow(engine, ready, event, policy, allowSilent, immediate) { pending = it }) {
                    playback.scheduleScreenOff(event, policy, { engine }, { ready }) { pending = it }
                }
            }
        }
    }

    fun interrupt() {
        playback.stop()
        engine?.stop()
        abandonFocus()
    }

    fun shutdown() {
        playback.stop()
        abandonFocus()
        engine?.shutdown()
        engine = null
        ready = false
    }

    private fun abandonFocus() {
        TtsEngine.abandonFocus(audio, focusRequest)
        focusRequest = null
    }
}
