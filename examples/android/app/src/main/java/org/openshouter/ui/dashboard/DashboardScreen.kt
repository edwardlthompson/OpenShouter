package org.openshouter.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingLg
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@Composable
fun DashboardScreen(
    announcerEnabled: Boolean,
    onAnnouncerChange: (Boolean) -> Unit,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd)
            .bottomInsetPadding(),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
            Text(stringResource(R.string.app_greeting), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.app_pitch), style = MaterialTheme.typography.bodyLarge)
        }
        MenuSection(stringResource(R.string.menu_section_master)) {
            MenuToggle(stringResource(R.string.dashboard_master), announcerEnabled, onAnnouncerChange)
        }
        MenuSection(stringResource(R.string.menu_section_hear)) {
            MenuLink(stringResource(R.string.nav_rules), onOpenRules)
            MenuLink(stringResource(R.string.announcer_title), onOpenAnnouncerSettings, stringResource(R.string.nav_announcer), true)
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
        }
    }
}
