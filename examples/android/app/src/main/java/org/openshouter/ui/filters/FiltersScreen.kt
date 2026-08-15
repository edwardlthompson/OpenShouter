package org.openshouter.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import org.openshouter.data.RegexEntity
import org.openshouter.domain.NotificationPolicy
import org.openshouter.domain.RegexAction
import org.openshouter.domain.RegexFilter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltersScreen(
    rules: List<RegexEntity>,
    policy: NotificationPolicy = NotificationPolicy(),
    onPolicy: (NotificationPolicy) -> Unit = {},
    onAdd: (pattern: String, action: String, replacement: String) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pattern by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var action by remember { mutableStateOf(RegexAction.IGNORE) }
    var requireText by remember { mutableStateOf("") }
    val ignoreRules = rules.filter { it.action == RegexAction.IGNORE.name }
    val replaceRules = rules.filter { it.action != RegexAction.IGNORE.name }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.nav_filters), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.filters_help), style = MaterialTheme.typography.bodyMedium)
        ToggleRow(stringResource(R.string.filters_ignore_empty), policy.ignoreEmpty) {
            onPolicy(policy.copy(ignoreEmpty = it))
        }
        ToggleRow(stringResource(R.string.filters_ignore_group), policy.ignoreGroup) {
            onPolicy(policy.copy(ignoreGroup = it))
        }
        ToggleRow(stringResource(R.string.filters_ignore_repeats), policy.ignoreRepeats) {
            onPolicy(policy.copy(ignoreRepeats = it))
        }
        OutlinedTextField(
            value = requireText,
            onValueChange = { requireText = it },
            label = { Text(stringResource(R.string.filters_require)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        OutlinedTextField(
            value = pattern,
            onValueChange = { pattern = it },
            label = { Text(stringResource(R.string.filters_pattern)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = replacement,
            onValueChange = { replacement = it },
            label = { Text(stringResource(R.string.filters_replacement)) },
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            RegexAction.entries.forEach { mode ->
                FilterChip(
                    selected = action == mode,
                    onClick = { action = mode },
                    label = {
                        Text(
                            when (mode) {
                                RegexAction.IGNORE -> stringResource(R.string.filters_action_ignore)
                                RegexAction.REPLACE -> stringResource(R.string.filters_action_replace)
                            },
                        )
                    },
                )
            }
        }
        Button(
            onClick = {
                addRequireLines(requireText, onAdd)
                requireText = ""
                if (pattern.isBlank() || pattern.length > RegexFilter.MAX_PATTERN) return@Button
                onAdd(pattern.trim(), action.name, replacement)
            },
        ) {
            Text(stringResource(R.string.filters_add))
        }
        RuleList(stringResource(R.string.filters_action_ignore), ignoreRules, onDelete)
        RuleList(stringResource(R.string.filters_action_replace), replaceRules, onDelete)
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}

private fun addRequireLines(
    text: String,
    onAdd: (pattern: String, action: String, replacement: String) -> Unit,
) {
    text.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && it.length <= RegexFilter.MAX_PATTERN }
        .forEach { onAdd(it, RegexAction.IGNORE.name, "") }
}

@Composable
private fun RuleList(
    title: String,
    rules: List<RegexEntity>,
    onDelete: (Long) -> Unit,
) {
    if (rules.isEmpty()) return
    Text(title, style = MaterialTheme.typography.titleMedium)
    rules.forEach { rule ->
        Text(stringResource(R.string.filters_item, rule.action, rule.pattern))
        Button(onClick = { onDelete(rule.id) }) {
            Text(stringResource(R.string.filters_delete))
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
