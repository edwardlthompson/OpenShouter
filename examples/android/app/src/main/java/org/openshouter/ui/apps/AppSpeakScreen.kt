package org.openshouter.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.apps.InstalledApp
import org.openshouter.domain.AppSettings
import org.openshouter.domain.AppSpeakList
import org.openshouter.domain.AppSpeakRule
import org.openshouter.domain.TtsFormat
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuClose
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle
import org.openshouter.ui.menu.highRefreshScroll
import org.openshouter.ui.menu.rememberMenuListScroll

@Composable
fun AppSpeakScreen(
    settings: AppSettings,
    rules: List<AppSpeakRule>,
    apps: List<InstalledApp>,
    onFormatChange: (String) -> Unit,
    onRuleChange: (String, Boolean, Boolean) -> Unit,
    onBulkChange: (List<String>, Boolean, Boolean) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    var format by remember(settings.ttsFormat) { mutableStateOf(settings.ttsFormat) }
    var query by remember { mutableStateOf("") }
    var selectedOnly by remember { mutableStateOf(false) }
    val byPackage = remember(rules) { rules.associateBy { it.packageName } }
    val filtered = remember(apps, query, selectedOnly, byPackage) {
        apps.filter { app ->
            AppSpeakList.include(app.label, app.packageName, query, selectedOnly, byPackage)
        }
    }
    val allSelected = remember(filtered, byPackage) {
        AppSpeakList.allSelected(filtered.map { it.packageName }, byPackage)
    }
    LazyColumn(
        state = rememberMenuListScroll(scrollStore, "apps"),
        modifier = modifier.highRefreshScroll().padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        item {
            Text(stringResource(R.string.nav_rules), style = MaterialTheme.typography.headlineSmall)
            MenuSection(stringResource(R.string.menu_section_actions)) {
                MenuBody {
                    Text(stringResource(R.string.apps_help), style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = format,
                        onValueChange = {
                            format = it
                            onFormatChange(it.ifBlank { TtsFormat.DEFAULT })
                        },
                        label = { Text(stringResource(R.string.rules_format)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(R.string.apps_search)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    MenuToggle(
                        label = stringResource(R.string.apps_filter_selected),
                        checked = selectedOnly,
                        onChange = { selectedOnly = it },
                    )
                    if (filtered.isNotEmpty()) {
                        MenuLink(
                            label = stringResource(
                                if (allSelected) R.string.apps_deselect_all else R.string.apps_select_all,
                            ),
                            onClick = {
                                onBulkChange(
                                    filtered.map { it.packageName },
                                    !allSelected,
                                    !allSelected,
                                )
                            },
                        )
                    }
                }
            }
        }
        if (filtered.isEmpty() && selectedOnly) {
            item {
                Text(
                    stringResource(R.string.apps_filter_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(filtered, key = { it.packageName }) { app ->
            val rule = byPackage[app.packageName]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                AppSpeakRow(
                    app = app,
                    speakAppName = rule?.speakAppName == true,
                    speakNotification = rule?.speakNotification == true,
                    onChange = onRuleChange,
                )
            }
        }
        item { MenuClose(onBack) }
    }
}

@Composable
private fun AppSpeakRow(
    app: InstalledApp,
    speakAppName: Boolean,
    speakNotification: Boolean,
    onChange: (String, Boolean, Boolean) -> Unit,
) {
    val nameLabel = stringResource(R.string.apps_toggle_name, app.label)
    val bodyLabel = stringResource(R.string.apps_toggle_notification, app.label)
    Column(modifier = Modifier.fillMaxWidth().padding(SpacingMd)) {
        Text(app.label, style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LabeledCheck(
                label = stringResource(R.string.apps_speak_name),
                description = nameLabel,
                checked = speakAppName,
                onChecked = { onChange(app.packageName, it, speakNotification) },
            )
            LabeledCheck(
                label = stringResource(R.string.apps_speak_notification),
                description = bodyLabel,
                checked = speakNotification,
                onChecked = { onChange(app.packageName, speakAppName, it) },
            )
        }
    }
}

@Composable
private fun LabeledCheck(
    label: String,
    description: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked,
            modifier = Modifier.semantics { contentDescription = description },
        )
        Text(label)
    }
}
