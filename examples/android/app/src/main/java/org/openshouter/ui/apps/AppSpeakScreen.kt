package org.openshouter.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.apps.InstalledApp
import org.openshouter.domain.AppSettings
import org.openshouter.domain.AppSpeakRule
import org.openshouter.domain.TtsFormat

@Composable
fun AppSpeakScreen(
    settings: AppSettings,
    rules: List<AppSpeakRule>,
    apps: List<InstalledApp>,
    onFormatChange: (String) -> Unit,
    onRuleChange: (String, Boolean, Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var format by remember(settings.ttsFormat) { mutableStateOf(settings.ttsFormat) }
    var query by remember { mutableStateOf("") }
    val byPackage = remember(rules) { rules.associateBy { it.packageName } }
    val filtered = remember(apps, query) {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) apps
        else apps.filter {
            it.label.lowercase().contains(needle) || it.packageName.lowercase().contains(needle)
        }
    }
    LazyColumn(
        modifier = modifier.padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                Text(stringResource(R.string.nav_rules), style = MaterialTheme.typography.headlineSmall)
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
            }
        }
        items(filtered, key = { it.packageName }) { app ->
            val rule = byPackage[app.packageName]
            AppSpeakRow(
                app = app,
                speakAppName = rule?.speakAppName == true,
                speakNotification = rule?.speakNotification == true,
                onChange = onRuleChange,
            )
        }
        item {
            Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
                Text(stringResource(R.string.settings_close))
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
    Column(modifier = Modifier.fillMaxWidth()) {
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
