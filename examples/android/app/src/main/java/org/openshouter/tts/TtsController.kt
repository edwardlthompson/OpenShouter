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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openshouter.audio.AudioRouteMonitor
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsSourceCatalog
import org.openshouter.domain.TtsVoiceCandidate

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
    @Volatile private var boundEngine: String? = null
    @Volatile private var ready = false
    @Volatile private var pending: SpokenEvent? = null
    @Volatile private var focusRequest: AudioFocusRequest? = null
    private val _engineGen = MutableStateFlow(0)
    val engineGen = _engineGen
    private val playback = TtsPlayback(
        appContext,
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
        carMode = { route.carModeActive() },
    )

    fun languageTags(enginePackage: String = boundEngine.orEmpty()): List<String> {
        warmup(enginePackage)
        return TtsEngine.languageTags(engine)
    }

    fun voices(enginePackage: String = boundEngine.orEmpty()): List<TtsVoiceCandidate> {
        warmup(enginePackage)
        return TtsEngines.voices(engine)
    }

    fun installedEngines() = TtsEngines.installed(appContext)

    fun downloadOffers() = TtsSourceCatalog.missing(installedEngines().map { it.packageName }.toSet())

    fun warmup(enginePackage: String = boundEngine.orEmpty()) {
        val want = enginePackage.trim()
        if (engine != null && boundEngine == want) return
        val old = engine
        ready = false
        engine = null
        boundEngine = want
        engine = TtsEngine.create(appContext, want) { status ->
            if (status != TextToSpeech.SUCCESS && want.isNotEmpty()) {
                warmup("")
                return@create
            }
            ready = status == TextToSpeech.SUCCESS
            engine?.language = Locale.getDefault()
            engine?.setOnUtteranceProgressListener(progressListener)
            _engineGen.value += 1
            pending?.let { queued ->
                pending = null
                speak(queued)
            }
        }
        old?.shutdown()
    }

    fun speak(event: SpokenEvent, immediate: Boolean = false) {
        scope.launch {
            val snap = settings.snapshot()
            val policy = snap.ttsPlayback.clamp()
            val allowSilent = ChannelStates.allowSilentVibrate(snap, event.kind)
            if (!immediate && !(event.looping && playback.looping != null) && policy.delaySeconds > 0) {
                delay(policy.delaySeconds * 1000L)
            }
            withContext(Dispatchers.Main) {
                warmup(policy.voice.engine)
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
        playback.release()
        abandonFocus()
        engine?.shutdown()
        engine = null
        boundEngine = null
        ready = false
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit
        override fun onError(utteranceId: String?) {
            scope.launch(Dispatchers.Main.immediate) { playback.onSynthFailed(utteranceId) }
        }
        override fun onDone(utteranceId: String?) {
            scope.launch(Dispatchers.Main.immediate) {
                playback.playSynthesized(engine, ready, utteranceId)
            }
        }
    }

    private fun abandonFocus() {
        TtsEngine.abandonFocus(audio, focusRequest)
        focusRequest = null
    }
}
