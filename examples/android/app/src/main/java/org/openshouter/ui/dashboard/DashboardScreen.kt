package org.openshouter.ui.dashboard

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
import dev.foss.goldenpath.ui.theme.SpacingMd

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
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.app_greeting), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.app_pitch), style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            val master = stringResource(R.string.dashboard_master)
            Text(master, modifier = Modifier.weight(1f))
            Switch(
                checked = announcerEnabled,
                onCheckedChange = onAnnouncerChange,
                modifier = Modifier.semantics { contentDescription = master },
            )
        }
        Button(onClick = onOpenSetup) { Text(stringResource(R.string.dashboard_open_setup)) }
        Button(onClick = onOpenRules) { Text(stringResource(R.string.nav_rules)) }
        Button(onClick = onOpenAnnouncerSettings) { Text(stringResource(R.string.nav_announcer)) }
        Button(onClick = onOpenHistory) { Text(stringResource(R.string.nav_history)) }
        Button(onClick = onOpenFilters) { Text(stringResource(R.string.nav_filters)) }
        Button(onClick = onOpenTts) { Text(stringResource(R.string.nav_tts)) }
        Button(onClick = onOpenReminders) { Text(stringResource(R.string.nav_reminders)) }
        Button(onClick = onOpenBackup) { Text(stringResource(R.string.nav_backup)) }
        Button(onClick = onOpenOverrides) { Text(stringResource(R.string.nav_overrides)) }
        Button(onClick = onOpenPlaces) { Text(stringResource(R.string.nav_places)) }
        Button(onClick = onOpenOem) { Text(stringResource(R.string.oem_title)) }
    }
}
