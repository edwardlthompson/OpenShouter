package org.openshouter.tts

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @Volatile private var pending: SpokenEvent? = null
    @Volatile private var focusRequest: AudioFocusRequest? = null
    @Volatile private var speakGen = 0L
    private val playback = TtsPlayback(
        appContext, audio,
        appContext.getSystemService(Context.POWER_SERVICE) as PowerManager,
        settings, scope, appContext.cacheDir,
        abandonFocus = { abandonFocus() },
        requestFocus = { policy, stream, car ->
            if (policy.audioFocus) {
                focusRequest = TtsEngine.requestFocus(audio, policy.pauseMedia, stream, car)
            }
        },
        isSilent = { route.isSilent() },
        carMode = { route.carModeActive() },
    )
    private lateinit var binder: TtsEngineBinder
    val engineGen get() = binder.engineGen
    val bindPhase get() = binder.bindPhase

    init {
        binder = TtsEngineBinder(
            appContext,
            TtsUtteranceCallbacks.listener(
                scope, audio, playback, { binder.engine }, { binder.ready }, ::interrupt,
            ),
            onReady = {
                pending?.let { queued ->
                    pending = null
                    speak(queued)
                }
            },
        )
    }

    private fun isSpeakCurrent(started: Long): Boolean = SpeakGeneration.isCurrent(speakGen, started)

    fun languageTags(enginePackage: String = binder.boundEngine.orEmpty()): List<String> {
        warmup(enginePackage)
        return TtsEngine.languageTags(binder.engine)
    }

    fun voices(enginePackage: String = binder.boundEngine.orEmpty()): List<TtsVoiceCandidate> {
        warmup(enginePackage)
        return TtsEngines.voices(binder.engine)
    }

    fun installedEngines() = TtsEngines.installed(appContext)

    fun downloadOffers() = TtsSourceCatalog.missing(installedEngines().map { it.packageName }.toSet())

    fun warmup(enginePackage: String = binder.boundEngine.orEmpty()) = binder.warmup(enginePackage)

    fun speak(event: SpokenEvent, immediate: Boolean = false) {
        val started = speakGen
        scope.launch {
            val snap = settings.snapshot()
            val policy = snap.ttsPlayback.clamp()
            val allowSilent = ChannelStates.allowPlaybackWhenSilent(snap, event.kind)
            if (!immediate && !(event.looping && playback.looping != null) && policy.delaySeconds > 0) {
                delay(policy.delaySeconds * 1000L)
            }
            if (!isSpeakCurrent(started)) return@launch
            withContext(Dispatchers.Main) {
                if (!isSpeakCurrent(started)) return@withContext
                warmup(policy.voice.engine)
                if (playback.speakNow(
                        binder.engine, binder.ready, event, policy, allowSilent, immediate, started,
                        ::isSpeakCurrent,
                    ) { pending = it }
                ) {
                    playback.scheduleScreenOff(
                        event, policy, { binder.engine }, { binder.ready }, started, ::isSpeakCurrent,
                    ) { pending = it }
                }
            }
        }
    }

    fun interrupt() {
        speakGen = SpeakGeneration.bump(speakGen)
        pending = null
        playback.stop()
        binder.engine?.stop()
        abandonFocus()
    }

    fun shutdown() {
        playback.release()
        abandonFocus()
        binder.shutdown()
    }

    private fun abandonFocus() {
        TtsEngine.abandonFocus(audio, focusRequest)
        focusRequest = null
    }
}
