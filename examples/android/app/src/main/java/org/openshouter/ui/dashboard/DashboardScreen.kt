package org.openshouter.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.tts.TtsBindPhase
import org.openshouter.updates.AstroAlarmLinks
import org.openshouter.updates.DonateLinks
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingLg
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle
import org.openshouter.ui.menu.highRefreshScroll
import org.openshouter.ui.menu.rememberMenuScroll

@Composable
fun DashboardScreen(
    announcerEnabled: Boolean,
    onAnnouncerChange: (Boolean) -> Unit,
    ttsBindPhase: TtsBindPhase = TtsBindPhase.Ready,
    onOpenSetup: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenAnnouncerSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenTts: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenOverrides: () -> Unit,
    onOpenPlaces: () -> Unit,
    onOpenOem: () -> Unit,
    onOpenSilence: () -> Unit,
    onOpenSettings: () -> Unit = {},
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier
            .highRefreshScroll()
            .verticalScroll(rememberMenuScroll(scrollStore, "home"))
            .padding(SpacingMd)
            .bottomInsetPadding(),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        MenuSection(stringResource(R.string.menu_section_master)) {
            MenuToggle(stringResource(R.string.dashboard_master), announcerEnabled, onAnnouncerChange)
            if (ttsBindPhase != TtsBindPhase.Ready) {
                MenuBody {
                    Text(
                        stringResource(
                            if (ttsBindPhase == TtsBindPhase.RetryOnce) {
                                R.string.tts_engine_retrying
                            } else {
                                R.string.tts_engine_failed
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        MenuSection(stringResource(R.string.menu_section_hear)) {
            MenuLink(stringResource(R.string.nav_rules), onOpenRules)
            MenuLink(stringResource(R.string.announcer_title), onOpenAnnouncerSettings, stringResource(R.string.nav_announcer), true)
            MenuLink(
                stringResource(R.string.nav_silence),
                onOpenSilence,
                stringResource(R.string.silence_help_short),
                true,
            )
            MenuLink(stringResource(R.string.tts_title), onOpenTts, stringResource(R.string.nav_tts), true)
            MenuLink(stringResource(R.string.nav_filters), onOpenFilters, showDivider = true)
            MenuLink(stringResource(R.string.nav_overrides), onOpenOverrides, showDivider = true)
        }
        MenuSection(stringResource(R.string.menu_section_log)) {
            MenuLink(stringResource(R.string.nav_history), onOpenHistory)
            MenuLink(stringResource(R.string.nav_reminders), onOpenReminders, showDivider = true)
        }
        MenuSection(stringResource(R.string.menu_section_phone)) {
            MenuLink(stringResource(R.string.nav_places), onOpenPlaces)
            MenuLink(stringResource(R.string.oem_title), onOpenOem, showDivider = true)
            MenuLink(stringResource(R.string.nav_backup), onOpenBackup, showDivider = true)
            MenuLink(stringResource(R.string.dashboard_open_setup), onOpenSetup, showDivider = true)
            MenuLink(
                stringResource(R.string.settings_title),
                onOpenSettings,
                stringResource(R.string.settings_appearance),
                true,
            )
            MenuLink(
                stringResource(R.string.astroalarm_promo_title),
                { uriHandler.openUri(AstroAlarmLinks.GITHUB_URL) },
                stringResource(R.string.astroalarm_promo_subtitle),
                true,
            )
            MenuLink(
                stringResource(R.string.about_donate),
                { uriHandler.openUri(DonateLinks.VENMO_URL) },
                showDivider = true,
            )
        }
    }
}
