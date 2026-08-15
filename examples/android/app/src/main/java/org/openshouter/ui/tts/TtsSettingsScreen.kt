package org.openshouter.ui.tts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.AppSettings
import org.openshouter.domain.ChannelDeviceState
import org.openshouter.domain.DeviceStatePolicy
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.TtsPlaybackPolicy
import org.openshouter.domain.TtsStream
import org.openshouter.domain.TtsVoice
import org.openshouter.ui.channel.ChannelStateScreen
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TtsSettingsScreen(
    settings: AppSettings,
    onPlayback: (TtsPlaybackPolicy) -> Unit,
    onDeviceState: (DeviceStatePolicy) -> Unit,
    onTest: () -> Unit,
    onPostTest: () -> Unit,
    onOpenSystemTts: () -> Unit,
    languages: List<String> = emptyList(),
    onChannelStates: (Map<ShoutChannel, ChannelDeviceState>) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showChannels by remember { mutableStateOf(false) }
    if (showChannels) {
        ChannelStateScreen(
            settings = settings,
            onSave = onChannelStates,
            onBack = { showChannels = false },
            modifier = modifier,
        )
        return
    }
    val playback = settings.ttsPlayback
    val device = settings.deviceState
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.nav_tts), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.tts_stream), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            StreamChip(TtsStream.NOTIFICATION, R.string.tts_stream_notification, playback, onPlayback)
            StreamChip(TtsStream.MEDIA, R.string.tts_stream_media, playback, onPlayback)
            StreamChip(TtsStream.ALARM, R.string.tts_stream_alarm, playback, onPlayback)
        }
        NudgeRow(stringResource(R.string.tts_delay), playback.delaySeconds) { delta ->
            onPlayback(playback.copy(delaySeconds = playback.delaySeconds + delta).clamp())
        }
        NudgeRow(stringResource(R.string.tts_max_length), playback.maxLength, step = 10) { delta ->
            onPlayback(playback.copy(maxLength = playback.maxLength + delta).clamp())
        }
        NudgeRow(stringResource(R.string.tts_repeat), playback.repeatMinutes) { delta ->
            onPlayback(playback.copy(repeatMinutes = playback.repeatMinutes + delta).clamp())
        }
        ToggleRow(stringResource(R.string.tts_audio_focus), playback.audioFocus) {
            onPlayback(playback.copy(audioFocus = it).clamp())
        }
        ToggleRow(stringResource(R.string.tts_speak_emojis), playback.speakEmojis) {
            onPlayback(playback.copy(speakEmojis = it).clamp())
        }
        NudgeRow(stringResource(R.string.tts_pitch), formatPitch(playback.voice.pitch)) { sign ->
            val voice = TtsVoice(
                pitch = playback.voice.pitch + sign * 0.1f,
                languageTag = playback.voice.languageTag,
            ).clamp()
            onPlayback(playback.copy(voice = voice).clamp())
        }
        Text(stringResource(R.string.tts_language), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FilterChip(
                selected = playback.voice.languageTag.isBlank(),
                onClick = {
                    onPlayback(playback.copy(voice = playback.voice.copy(languageTag = "").clamp()).clamp())
                },
                label = { Text(stringResource(R.string.tts_language_default)) },
            )
            languages.forEach { tag ->
                FilterChip(
                    selected = playback.voice.languageTag == tag,
                    onClick = {
                        val voice = TtsVoice(pitch = playback.voice.pitch, languageTag = tag).clamp()
                        onPlayback(playback.copy(voice = voice).clamp())
                    },
                    label = { Text(tag) },
                )
            }
        }
        if (languages.isEmpty()) {
            OutlinedTextField(
                value = playback.voice.languageTag,
                onValueChange = { tag ->
                    val voice = TtsVoice(pitch = playback.voice.pitch, languageTag = tag).clamp()
                    onPlayback(playback.copy(voice = voice).clamp())
                },
                label = { Text(stringResource(R.string.tts_language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        ToggleRow(stringResource(R.string.tts_pause), playback.pauseMedia) {
            onPlayback(playback.copy(pauseMedia = it).clamp())
        }
        NudgeRow(stringResource(R.string.tts_repeat_count), playback.repeatCount) { delta ->
            onPlayback(playback.copy(repeatCount = playback.repeatCount + delta).clamp())
        }
        Text(stringResource(R.string.tts_tokens), style = MaterialTheme.typography.bodySmall)
        ToggleRow(stringResource(R.string.tts_device_screen_on), device.allowScreenOn) {
            onDeviceState(device.copy(allowScreenOn = it))
        }
        ToggleRow(stringResource(R.string.tts_device_screen_off), device.allowScreenOff) {
            onDeviceState(device.copy(allowScreenOff = it))
        }
        ToggleRow(stringResource(R.string.tts_device_headset_on), device.allowHeadsetOn) {
            onDeviceState(device.copy(allowHeadsetOn = it))
        }
        ToggleRow(stringResource(R.string.tts_device_headset_off), device.allowHeadsetOff) {
            onDeviceState(device.copy(allowHeadsetOff = it))
        }
        ToggleRow(stringResource(R.string.tts_device_silent), device.allowSilentVibrate) {
            onDeviceState(device.copy(allowSilentVibrate = it))
        }
        ToggleRow(stringResource(R.string.tts_device_incall), device.allowInCall) {
            onDeviceState(device.copy(allowInCall = it))
        }
        Button(onClick = { showChannels = true }) { Text(stringResource(R.string.nav_channels)) }
        Button(onClick = onTest) { Text(stringResource(R.string.tts_test)) }
        Button(onClick = onPostTest) { Text(stringResource(R.string.tts_test_notification)) }
        Button(onClick = onOpenSystemTts) { Text(stringResource(R.string.tts_system)) }
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}

@Composable
private fun StreamChip(
    stream: TtsStream,
    labelRes: Int,
    playback: TtsPlaybackPolicy,
    onPlayback: (TtsPlaybackPolicy) -> Unit,
) {
    FilterChip(
        selected = playback.stream == stream,
        onClick = { onPlayback(playback.copy(stream = stream).clamp()) },
        label = { Text(stringResource(labelRes)) },
    )
}

@Composable
private fun NudgeRow(label: String, value: Int, step: Int = 1, onNudge: (Int) -> Unit) {
    NudgeRow(label, value.toString()) { sign -> onNudge(sign * step) }
}

@Composable
private fun NudgeRow(label: String, display: String, onNudge: (Int) -> Unit) {
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

private fun formatPitch(pitch: Float): String = "%.1f".format(Locale.US, pitch)

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            modifier = Modifier.semantics { contentDescription = label },
        )
    }
}
