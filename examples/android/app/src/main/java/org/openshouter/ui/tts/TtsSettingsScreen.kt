package org.openshouter.ui.tts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.AppSettings
import org.openshouter.domain.ChannelDeviceState
import org.openshouter.domain.DeviceStatePolicy
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.TtsFormat
import org.openshouter.domain.TtsPlaybackPolicy
import org.openshouter.domain.TtsEngineChoice
import org.openshouter.domain.TtsSourceOffer
import org.openshouter.domain.TtsStream
import org.openshouter.domain.TtsVoiceCandidate
import org.openshouter.ui.channel.ChannelStateScreen
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuDropdown
import org.openshouter.ui.menu.MenuToggle
import java.util.Locale

@Composable
fun TtsSettingsScreen(
    settings: AppSettings,
    onPlayback: (TtsPlaybackPolicy) -> Unit,
    onFormatChange: (String) -> Unit = {},
    onDeviceState: (DeviceStatePolicy) -> Unit,
    onTest: () -> Unit,
    onPostTest: () -> Unit,
    onOpenSystemTts: () -> Unit,
    languages: List<String> = emptyList(),
    voices: List<TtsVoiceCandidate> = emptyList(),
    engineGen: StateFlow<Int>? = null,
    loadLanguages: (() -> List<String>)? = null,
    loadVoices: (() -> List<TtsVoiceCandidate>)? = null,
    engines: List<TtsEngineChoice> = emptyList(),
    downloads: List<TtsSourceOffer> = emptyList(),
    onOpenUrl: (String) -> Unit = {},
    onOpenApp: (String) -> Unit = {},
    onChannelStates: (Map<ShoutChannel, ChannelDeviceState>) -> Unit = {},
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    var format by remember(settings.ttsFormat) { mutableStateOf(settings.ttsFormat) }
    var showChannels by remember { mutableStateOf(false) }
    if (showChannels) {
        BackHandler { showChannels = false }
        ChannelStateScreen(
            settings = settings,
            onSave = onChannelStates,
            onBack = { showChannels = false },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        return
    }
    val playback = settings.ttsPlayback
    val device = settings.deviceState
    val fallbackGen = remember { MutableStateFlow(0) }
    val catalogTick by (engineGen ?: fallbackGen).collectAsStateWithLifecycle(0)
    val resolvedLangs = remember(catalogTick, languages) { loadLanguages?.invoke() ?: languages }
    val resolvedVoices = remember(catalogTick, voices) { loadVoices?.invoke() ?: voices }
    MenuScaffold(stringResource(R.string.tts_title), scrollStore, "tts", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            MenuBody {
                OutlinedTextField(
                    value = format,
                    onValueChange = {
                        format = it.take(TtsFormat.MAX_TEMPLATE)
                        onFormatChange(TtsFormat.clamp(format))
                    },
                    label = { Text(stringResource(R.string.rules_format)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.tts_tokens), style = MaterialTheme.typography.bodySmall)
            }
        }
        MenuSection(stringResource(R.string.menu_section_voice)) {
            MenuBody {
                val streams = listOf(
                    TtsStream.NOTIFICATION to stringResource(R.string.tts_stream_notification),
                    TtsStream.MEDIA to stringResource(R.string.tts_stream_media),
                    TtsStream.ALARM to stringResource(R.string.tts_stream_alarm),
                )
                MenuDropdown(
                    label = stringResource(R.string.tts_stream),
                    text = streams.firstOrNull { it.first == playback.stream }?.second ?: streams[1].second,
                    options = streams.map { it.first.name to it.second },
                    onSelect = { name ->
                        val stream = runCatching { TtsStream.valueOf(name) }.getOrDefault(TtsStream.MEDIA)
                        onPlayback(playback.copy(stream = stream).clamp())
                    },
                )
                TtsVoicePicker(
                    playback = playback,
                    onPlayback = onPlayback,
                    languages = resolvedLangs,
                    voices = resolvedVoices,
                    engines = engines,
                    downloads = downloads,
                    onOpenUrl = onOpenUrl,
                    onOpenApp = onOpenApp,
                )
            }
            MenuToggle(
                label = stringResource(R.string.tts_pause),
                checked = playback.pauseMedia,
                onChange = { onPlayback(playback.copy(pauseMedia = it).clamp()) },
                showDivider = true,
            )
        }
        MenuSection(stringResource(R.string.menu_section_timing)) {
            MenuBody {
                NudgeRow(stringResource(R.string.tts_delay), playback.delaySeconds) { delta ->
                    onPlayback(playback.copy(delaySeconds = playback.delaySeconds + delta).clamp())
                }
                NudgeRow(stringResource(R.string.tts_max_length), playback.maxLength, step = 10) { delta ->
                    onPlayback(playback.copy(maxLength = playback.maxLength + delta).clamp())
                }
                NudgeRow(stringResource(R.string.tts_repeat), playback.repeatMinutes) { delta ->
                    onPlayback(playback.copy(repeatMinutes = playback.repeatMinutes + delta).clamp())
                }
                NudgeRow(stringResource(R.string.tts_repeat_count), playback.repeatCount) { delta ->
                    onPlayback(playback.copy(repeatCount = playback.repeatCount + delta).clamp())
                }
            }
            MenuToggle(
                label = stringResource(R.string.tts_audio_focus),
                checked = playback.audioFocus,
                onChange = { onPlayback(playback.copy(audioFocus = it).clamp()) },
                showDivider = true,
            )
            MenuToggle(
                label = stringResource(R.string.tts_speak_emojis),
                checked = playback.speakEmojis,
                onChange = { onPlayback(playback.copy(speakEmojis = it).clamp()) },
                showDivider = true,
            )
        }
        MenuSection(stringResource(R.string.menu_section_states)) {
            MenuToggle(
                label = stringResource(R.string.tts_device_screen_on),
                checked = device.allowScreenOn,
                onChange = { onDeviceState(device.copy(allowScreenOn = it)) },
            )
            MenuToggle(
                label = stringResource(R.string.tts_device_screen_off),
                checked = device.allowScreenOff,
                onChange = { onDeviceState(device.copy(allowScreenOff = it)) },
                showDivider = true,
            )
            MenuToggle(
                label = stringResource(R.string.tts_device_headset_on),
                checked = device.allowHeadsetOn,
                onChange = { onDeviceState(device.copy(allowHeadsetOn = it)) },
                showDivider = true,
            )
            MenuToggle(
                label = stringResource(R.string.tts_device_headset_off),
                checked = device.allowHeadsetOff,
                onChange = { onDeviceState(device.copy(allowHeadsetOff = it)) },
                showDivider = true,
            )
            MenuToggle(
                label = stringResource(R.string.tts_device_silent),
                checked = device.allowSilentVibrate,
                onChange = { onDeviceState(device.copy(allowSilentVibrate = it)) },
                showDivider = true,
            )
            MenuToggle(
                label = stringResource(R.string.tts_device_incall),
                checked = device.allowInCall,
                onChange = { onDeviceState(device.copy(allowInCall = it)) },
                showDivider = true,
            )
            MenuLink(
                label = stringResource(R.string.nav_channels),
                onClick = { showChannels = true },
                showDivider = true,
            )
        }
        MenuSection(stringResource(R.string.menu_section_try)) {
            MenuLink(label = stringResource(R.string.tts_test), onClick = onTest)
            MenuLink(label = stringResource(R.string.tts_test_notification), onClick = onPostTest, showDivider = true)
            MenuLink(label = stringResource(R.string.tts_system), onClick = onOpenSystemTts, showDivider = true)
        }
    }
}

@Composable
internal fun NudgeRow(label: String, value: Int, step: Int = 1, onNudge: (Int) -> Unit) {
    NudgeRow(label, value.toString()) { sign -> onNudge(sign * step) }
}

@Composable
internal fun NudgeRow(label: String, display: String, onNudge: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Button(onClick = { onNudge(-1) }, modifier = Modifier.semantics { contentDescription = label }) {
            Text(stringResource(R.string.quiet_minus))
        }
        Text(display, style = MaterialTheme.typography.titleMedium)
        Button(onClick = { onNudge(1) }, modifier = Modifier.semantics { contentDescription = label }) {
            Text(stringResource(R.string.quiet_plus))
        }
    }
}

internal fun formatPitch(pitch: Float): String = "%.1f".format(Locale.US, pitch)
