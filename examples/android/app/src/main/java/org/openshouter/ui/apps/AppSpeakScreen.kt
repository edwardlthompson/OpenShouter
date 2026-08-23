package org.openshouter.ui.apps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.apps.InstalledApp
import org.openshouter.domain.AppSpeakList
import org.openshouter.domain.AppSpeakRule
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.highRefreshScroll
import org.openshouter.ui.menu.rememberMenuListScroll

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppSpeakScreen(
    rules: List<AppSpeakRule>,
    apps: List<InstalledApp>,
    onRuleChange: (String, Boolean, Boolean) -> Unit,
    onBulkChange: (List<String>, Boolean, Boolean) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var selectedOnly by remember { mutableStateOf(false) }
    val searchFocus = remember { FocusRequester() }
    val byPackage = remember(rules) { rules.associateBy { it.packageName } }
    val filtered = remember(apps, query, selectedOnly, byPackage) {
        apps.filter { app ->
            AppSpeakList.include(app.label, app.packageName, query, selectedOnly, byPackage)
        }
    }
    val allSelected = remember(filtered, byPackage) {
        AppSpeakList.allSelected(filtered.map { it.packageName }, byPackage)
    }
    fun closeSearch() {
        searching = false
        query = ""
    }
    BackHandler {
        if (searching) closeSearch() else onBack()
    }
    LaunchedEffect(searching) {
        if (searching) runCatching { searchFocus.requestFocus() }
    }
    Column(
        modifier = modifier.fillMaxSize().padding(start = SpacingMd, top = SpacingMd, end = SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.nav_rules),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { if (searching) closeSearch() else searching = true }) {
                Icon(
                    imageVector = if (searching) Icons.Filled.Close else Icons.Filled.Search,
                    contentDescription = stringResource(
                        if (searching) R.string.apps_search_close else R.string.apps_search,
                    ),
                )
            }
        }
        if (searching) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.apps_search)) },
                modifier = Modifier.fillMaxWidth().focusRequester(searchFocus),
                singleLine = true,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
            verticalArrangement = Arrangement.Center,
        ) {
            FilterChip(
                selected = selectedOnly,
                onClick = { selectedOnly = !selectedOnly },
                label = { Text(stringResource(R.string.apps_filter_selected)) },
            )
            if (filtered.isNotEmpty()) {
                TextButton(
                    onClick = {
                        onBulkChange(
                            filtered.map { it.packageName },
                            !allSelected,
                            !allSelected,
                        )
                    },
                ) {
                    Text(
                        stringResource(
                            if (allSelected) R.string.apps_deselect_all else R.string.apps_select_all,
                        ),
                    )
                }
            }
        }
        LazyColumn(
            state = rememberMenuListScroll(scrollStore, "apps"),
            contentPadding = PaddingValues(bottom = SpacingMd),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .highRefreshScroll(),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
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
        }
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
