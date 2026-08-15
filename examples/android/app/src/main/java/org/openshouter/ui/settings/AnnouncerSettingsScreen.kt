package org.openshouter.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.AppSettings
import org.openshouter.domain.QuietHours
import org.openshouter.gesture.ShakeSettings

@Composable
fun AnnouncerSettingsScreen(
    settings: AppSettings,
    onQuiet: (Boolean) -> Unit,
    onScreenOffOnly: (Boolean) -> Unit,
    onHeadsetOnly: (Boolean) -> Unit,
    onShake: (Boolean) -> Unit,
    onShakeThreshold: (Float) -> Unit,
    onFlip: (Boolean) -> Unit,
    onMuteScreenOn: (Boolean) -> Unit,
    onMuteScreenOff: (Boolean) -> Unit,
    onCalls: (Boolean) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onTimeShout: (Boolean) -> Unit,
    onMissed: (Boolean) -> Unit,
    onMessages: (Boolean) -> Unit,
    onOpenQuiet: () -> Unit,
    onOpenTime: () -> Unit,
    onOpenContacts: () -> Unit = {},
    onOpenMessages: () -> Unit = {},
    onOpenPower: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.nav_announcer), style = MaterialTheme.typography.headlineSmall)
        ToggleRow(stringResource(R.string.announcer_notifications), settings.notificationsEnabled, onNotifications)
        ToggleRow(stringResource(R.string.announcer_calls), settings.callsEnabled, onCalls)
        ToggleRow(stringResource(R.string.announcer_time), settings.timeShoutEnabled, onTimeShout)
        Button(onClick = onOpenTime) { Text(stringResource(R.string.announcer_time_customize)) }
        ToggleRow(stringResource(R.string.announcer_missed), settings.missedCall.enabled, onMissed)
        ToggleRow(stringResource(R.string.announcer_messages), settings.messageChannel.enabled, onMessages)
        Button(onClick = onOpenContacts) { Text(stringResource(R.string.nav_contacts)) }
        Button(onClick = onOpenMessages) { Text(stringResource(R.string.nav_messages)) }
        Button(onClick = onOpenPower) { Text(stringResource(R.string.nav_power)) }
        ToggleRow(
            stringResource(
                R.string.announcer_quiet,
                QuietHours.windowLabel(settings.quietStartMinutes, settings.quietEndMinutes),
            ),
            settings.quietHoursEnabled,
            onQuiet,
        )
        Button(onClick = onOpenQuiet) {
            Text(stringResource(R.string.announcer_quiet_customize))
        }
        ToggleRow(stringResource(R.string.announcer_screen_off), settings.screenOffOnly, onScreenOffOnly)
        ToggleRow(stringResource(R.string.announcer_headset), settings.headsetOnly, onHeadsetOnly)
        ToggleRow(stringResource(R.string.announcer_shake), settings.shakeToSilence, onShake)
        if (settings.shakeToSilence) {
            ShakeSettings(settings.shakeThreshold, onShakeThreshold)
        }
        ToggleRow(stringResource(R.string.announcer_flip), settings.flipToMute, onFlip)
        ToggleRow(stringResource(R.string.announcer_mute_on), settings.muteOnScreenOn, onMuteScreenOn)
        ToggleRow(stringResource(R.string.announcer_mute_off), settings.muteOnScreenOff, onMuteScreenOff)
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}

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
