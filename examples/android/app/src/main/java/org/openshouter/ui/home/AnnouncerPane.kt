package org.openshouter.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.openshouter.domain.AppSettings
import org.openshouter.service.OpenShouterEntryPoint
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.settings.AnnouncerSettingsScreen

@Composable
fun AnnouncerPane(
    settings: AppSettings,
    ep: OpenShouterEntryPoint,
    scope: CoroutineScope,
    onPane: (Pane) -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    AnnouncerSettingsScreen(
        settings = settings,
        onQuiet = { on ->
            scope.launch {
                ep.settings().setQuietHours(
                    on,
                    settings.quietStartMinutes,
                    settings.quietEndMinutes,
                    settings.quietDays,
                )
            }
        },
        onScreenOffOnly = { on ->
            scope.launch { ep.settings().setAudioGate(on, settings.headsetOnly) }
        },
        onHeadsetOnly = { on ->
            scope.launch { ep.settings().setAudioGate(settings.screenOffOnly, on) }
        },
        onShake = { on ->
            scope.launch {
                ep.settings().setGestures(on, settings.flipToMute, settings.muteOnScreenOn, settings.muteOnScreenOff)
            }
        },
        onShakeThreshold = { g -> scope.launch { ep.settings().setShakeThreshold(g) } },
        onFlip = { on ->
            scope.launch {
                ep.settings().setGestures(settings.shakeToSilence, on, settings.muteOnScreenOn, settings.muteOnScreenOff)
            }
        },
        onMuteScreenOn = { on ->
            scope.launch {
                ep.settings().setGestures(settings.shakeToSilence, settings.flipToMute, on, settings.muteOnScreenOff)
            }
        },
        onMuteScreenOff = { on ->
            scope.launch {
                ep.settings().setGestures(settings.shakeToSilence, settings.flipToMute, settings.muteOnScreenOn, on)
            }
        },
        onCalls = { on -> scope.launch { ep.settings().setCalls(on) } },
        onNotifications = { on -> scope.launch { ep.settings().setNotifications(on) } },
        onTimeShout = { on ->
            scope.launch {
                ep.settings().setTimeShout(on, settings.timeShoutIntervalMinutes, settings.timeShoutExact)
            }
        },
        onMissed = { on ->
            scope.launch { ep.settings().setMissedCall(settings.missedCall.copy(enabled = on)) }
        },
        onMessages = { on ->
            scope.launch { ep.settings().setMessageChannel(settings.messageChannel.copy(enabled = on)) }
        },
        onOpenQuiet = { onPane(Pane.Quiet) },
        onOpenTime = { onPane(Pane.Time) },
        onOpenContacts = { onPane(Pane.Contacts) },
        onOpenMessages = { onPane(Pane.Messages) },
        onOpenPower = { onPane(Pane.Power) },
        onOpenCalendar = { onPane(Pane.Calendar) },
        onOpenBluetooth = { onPane(Pane.Bluetooth) },
        onCalendar = { on -> scope.launch { ep.sprint15().setCalendar(on) } },
        onBluetoothConnect = { on ->
            scope.launch { ep.sprint15().setBluetooth(on, settings.bluetoothBatteryAlert) }
        },
        onBack = { onPane(Pane.Home) },
        scrollStore = scrollStore,
        modifier = modifier,
    )
}
