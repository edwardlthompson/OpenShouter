package org.openshouter.ui.channel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.AppSettings
import org.openshouter.domain.ChannelDeviceState
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.TtsStream
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuDropdown
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@Composable
fun ChannelStateScreen(
    settings: AppSettings,
    onSave: (Map<ShoutChannel, ChannelDeviceState>) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    var channel by remember { mutableStateOf(ShoutChannel.CALL) }
    val current = ChannelStates.resolve(
        settings.channelStates,
        channel,
        settings.deviceState,
        settings.ttsPlayback,
    )
    MenuScaffold(stringResource(R.string.nav_channels), scrollStore, "channels", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_states)) {
            MenuBody {
                val channels = ShoutChannel.entries.map { it.name to stringResource(channelLabel(it)) }
                MenuDropdown(
                    label = stringResource(R.string.nav_channels),
                    text = channels.firstOrNull { it.first == channel.name }?.second ?: channels.first().second,
                    options = channels,
                    onSelect = { name ->
                        channel = runCatching { ShoutChannel.valueOf(name) }.getOrDefault(ShoutChannel.CALL)
                    },
                )
            }
            MenuToggle(stringResource(R.string.tts_device_screen_on), current.device.allowScreenOn, {
                persist(settings, channel, current.copy(device = current.device.copy(allowScreenOn = it)), onSave)
            })
            MenuToggle(stringResource(R.string.tts_device_screen_off), current.device.allowScreenOff, {
                persist(settings, channel, current.copy(device = current.device.copy(allowScreenOff = it)), onSave)
            }, true)
            MenuToggle(stringResource(R.string.tts_device_headset_on), current.device.allowHeadsetOn, {
                persist(settings, channel, current.copy(device = current.device.copy(allowHeadsetOn = it)), onSave)
            }, true)
            MenuToggle(stringResource(R.string.tts_device_headset_off), current.device.allowHeadsetOff, {
                persist(settings, channel, current.copy(device = current.device.copy(allowHeadsetOff = it)), onSave)
            }, true)
            MenuToggle(stringResource(R.string.tts_device_silent), current.device.allowSilentVibrate, {
                persist(settings, channel, current.copy(device = current.device.copy(allowSilentVibrate = it)), onSave)
            }, true)
            MenuToggle(stringResource(R.string.tts_device_incall), current.device.allowInCall, {
                persist(settings, channel, current.copy(device = current.device.copy(allowInCall = it)), onSave)
            }, true)
            MenuBody {
                val streams = TtsStream.entries.map { it.name to stringResource(streamLabel(it)) }
                MenuDropdown(
                    label = stringResource(R.string.tts_stream),
                    text = streams.firstOrNull { it.first == current.stream.name }?.second ?: streams[1].second,
                    options = streams,
                    onSelect = { name ->
                        val stream = runCatching { TtsStream.valueOf(name) }.getOrDefault(TtsStream.MEDIA)
                        persist(settings, channel, current.copy(stream = stream), onSave)
                    },
                )
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
                AppNameCooldownDropdown(
                    seconds = current.appNameCooldownSeconds,
                    onChange = { seconds ->
                        persist(
                            settings,
                            channel,
                            current.copy(appNameCooldownSeconds = seconds).clamp(),
                            onSave,
                        )
                    },
                )
            }
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
    ShoutChannel.CALENDAR -> R.string.nav_calendar
    ShoutChannel.BLUETOOTH -> R.string.nav_bluetooth
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
