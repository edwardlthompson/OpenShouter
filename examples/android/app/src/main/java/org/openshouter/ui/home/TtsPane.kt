package org.openshouter.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.foss.goldenpath.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.openshouter.domain.AppSettings
import org.openshouter.domain.SpokenEvent
import org.openshouter.notification.TestNotification
import org.openshouter.service.OpenShouterEntryPoint
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.tts.TtsSettingsScreen

@Composable
fun TtsPane(
    settings: AppSettings,
    ep: OpenShouterEntryPoint,
    scope: CoroutineScope,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    TtsSettingsScreen(
        settings = settings,
        onPlayback = { policy -> scope.launch { ep.settings().setTtsPlayback(policy) } },
        onFormatChange = { value -> scope.launch { ep.settings().setFormat(value) } },
        onDeviceState = { policy -> scope.launch { ep.settings().setDeviceState(policy) } },
        onTest = {
            ep.tts().speak(
                SpokenEvent(
                    SpokenEvent.Kind.NOTIFICATION,
                    context.getString(R.string.tts_test_phrase),
                    stream = settings.ttsPlayback.stream,
                ),
                immediate = true,
            )
        },
        onPostTest = { TestNotification.post(context) },
        onOpenSystemTts = {
            runCatching {
                context.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        },
        languages = ep.tts().languageTags(settings.ttsPlayback.voice.engine),
        voices = ep.tts().voices(settings.ttsPlayback.voice.engine),
        engineGen = ep.tts().engineGen,
        loadLanguages = { ep.tts().languageTags(settings.ttsPlayback.voice.engine) },
        loadVoices = { ep.tts().voices(settings.ttsPlayback.voice.engine) },
        engines = ep.tts().installedEngines(),
        downloads = ep.tts().downloadOffers(),
        onOpenUrl = { url ->
            if (!url.startsWith("https://")) return@TtsSettingsScreen
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        },
        onOpenApp = { pkg ->
            val launch = context.packageManager.getLaunchIntentForPackage(pkg) ?: return@TtsSettingsScreen
            runCatching { context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        },
        onChannelStates = { map -> scope.launch { ep.sprint13().setChannelStates(map) } },
        onBack = onBack,
        scrollStore = scrollStore,
        modifier = modifier,
    )
}
