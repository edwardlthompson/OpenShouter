package org.openshouter.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.domain.AppSettings
import org.openshouter.domain.QuietHours
import org.openshouter.gesture.ShakeSettings
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuRule
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

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
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    MenuScaffold(stringResource(R.string.announcer_title), scrollStore, "announcer", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            MenuToggle(stringResource(R.string.announcer_notifications), settings.notificationsEnabled, onNotifications)
            MenuToggle(stringResource(R.string.announcer_calls), settings.callsEnabled, onCalls, true)
            MenuToggle(stringResource(R.string.announcer_time), settings.timeShoutEnabled, onTimeShout, true)
            MenuToggle(stringResource(R.string.announcer_missed), settings.missedCall.enabled, onMissed, true)
            MenuToggle(stringResource(R.string.announcer_messages), settings.messageChannel.enabled, onMessages, true)
        }
        MenuSection(stringResource(R.string.menu_section_more)) {
            MenuLink(stringResource(R.string.announcer_time_customize), onOpenTime)
            MenuLink(stringResource(R.string.nav_contacts), onOpenContacts, showDivider = true)
            MenuLink(stringResource(R.string.nav_messages), onOpenMessages, showDivider = true)
            MenuLink(stringResource(R.string.nav_power), onOpenPower, showDivider = true)
        }
        MenuSection(stringResource(R.string.menu_section_quiet)) {
            MenuToggle(
                stringResource(
                    R.string.announcer_quiet,
                    QuietHours.windowLabel(settings.quietStartMinutes, settings.quietEndMinutes),
                ),
                settings.quietHoursEnabled,
                onQuiet,
            )
            MenuLink(stringResource(R.string.announcer_quiet_customize), onOpenQuiet, showDivider = true)
        }
        MenuSection(stringResource(R.string.menu_section_when)) {
            MenuToggle(stringResource(R.string.announcer_screen_off), settings.screenOffOnly, onScreenOffOnly)
            MenuToggle(stringResource(R.string.announcer_headset), settings.headsetOnly, onHeadsetOnly, true)
        }
        MenuSection(stringResource(R.string.menu_section_silence)) {
            MenuToggle(stringResource(R.string.announcer_shake), settings.shakeToSilence, onShake)
            if (settings.shakeToSilence) {
                MenuRule()
                MenuBody { ShakeSettings(settings.shakeThreshold, onShakeThreshold) }
            }
            MenuToggle(stringResource(R.string.announcer_flip), settings.flipToMute, onFlip, true)
            MenuToggle(stringResource(R.string.announcer_mute_on), settings.muteOnScreenOn, onMuteScreenOn, true)
            MenuToggle(stringResource(R.string.announcer_mute_off), settings.muteOnScreenOff, onMuteScreenOff, true)
        }
    }
}
