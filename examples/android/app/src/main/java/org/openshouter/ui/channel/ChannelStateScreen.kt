package org.openshouter.ui.channel

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
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.TtsStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChannelStateScreen(
    settings: AppSettings,
    onSave: (Map<ShoutChannel, ChannelDeviceState>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var channel by remember { mutableStateOf(ShoutChannel.CALL) }
    val current = ChannelStates.resolve(
        settings.channelStates,
        channel,
        settings.deviceState,
        settings.ttsPlayback,
    )
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.nav_channels), style = MaterialTheme.typography.headlineSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ShoutChannel.entries.forEach { value ->
                FilterChip(
                    selected = channel == value,
                    onClick = { channel = value },
                    label = { Text(stringResource(channelLabel(value))) },
                )
            }
        }
        Toggle(stringResource(R.string.tts_device_screen_on), current.device.allowScreenOn) {
            persist(settings, channel, current.copy(device = current.device.copy(allowScreenOn = it)), onSave)
        }
        Toggle(stringResource(R.string.tts_device_screen_off), current.device.allowScreenOff) {
            persist(settings, channel, current.copy(device = current.device.copy(allowScreenOff = it)), onSave)
        }
        Toggle(stringResource(R.string.tts_device_headset_on), current.device.allowHeadsetOn) {
            persist(settings, channel, current.copy(device = current.device.copy(allowHeadsetOn = it)), onSave)
        }
        Toggle(stringResource(R.string.tts_device_headset_off), current.device.allowHeadsetOff) {
            persist(settings, channel, current.copy(device = current.device.copy(allowHeadsetOff = it)), onSave)
        }
        Toggle(stringResource(R.string.tts_device_silent), current.device.allowSilentVibrate) {
            persist(settings, channel, current.copy(device = current.device.copy(allowSilentVibrate = it)), onSave)
        }
        Toggle(stringResource(R.string.tts_device_incall), current.device.allowInCall) {
            persist(settings, channel, current.copy(device = current.device.copy(allowInCall = it)), onSave)
        }
        Text(stringResource(R.string.tts_stream), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            TtsStream.entries.forEach { stream ->
                FilterChip(
                    selected = current.stream == stream,
                    onClick = { persist(settings, channel, current.copy(stream = stream), onSave) },
                    label = { Text(stringResource(streamLabel(stream))) },
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Text(stringResource(R.string.tts_repeat_count), modifier = Modifier.weight(1f))
            Button(onClick = {
                persist(settings, channel, current.copy(repeatCount = current.repeatCount - 1).clamp(), onSave)
            }) { Text(stringResource(R.string.quiet_minus)) }
            Text(current.repeatCount.toString(), style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                persist(settings, channel, current.copy(repeatCount = current.repeatCount + 1).clamp(), onSave)
            }) { Text(stringResource(R.string.quiet_plus)) }
        }
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}

private fun channelLabel(channel: ShoutChannel): Int = when (channel) {
    ShoutChannel.NOTIFICATION -> R.string.announcer_notifications
    ShoutChannel.CALL -> R.string.announcer_calls
    ShoutChannel.MESSAGE -> R.string.announcer_messages
    ShoutChannel.TIME -> R.string.time_title
    ShoutChannel.BATTERY -> R.string.nav_power
    ShoutChannel.REMINDER -> R.string.nav_reminders
}

private fun streamLabel(stream: TtsStream): Int = when (stream) {
    TtsStream.NOTIFICATION -> R.string.tts_stream_notification
    TtsStream.MEDIA -> R.string.tts_stream_media
    TtsStream.ALARM -> R.string.tts_stream_alarm
}

private fun persist(
    settings: AppSettings,
    channel: ShoutChannel,
    state: ChannelDeviceState,
    onSave: (Map<ShoutChannel, ChannelDeviceState>) -> Unit,
) {
    onSave(settings.channelStates + (channel to state.clamp()))
}

@Composable
private fun Toggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
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
