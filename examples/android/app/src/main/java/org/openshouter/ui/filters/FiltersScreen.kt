package org.openshouter.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.data.RegexEntity
import org.openshouter.domain.NotificationPolicy
import org.openshouter.domain.RegexAction
import org.openshouter.domain.RegexFilter
import org.openshouter.domain.SpeakImportance
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltersScreen(
    rules: List<RegexEntity>,
    policy: NotificationPolicy = NotificationPolicy(),
    onPolicy: (NotificationPolicy) -> Unit = {},
    onAdd: (pattern: String, action: String, replacement: String) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    var pattern by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var action by remember { mutableStateOf(RegexAction.IGNORE) }
    var requireText by remember { mutableStateOf("") }
    val ignoreRules = rules.filter { it.action == RegexAction.IGNORE.name }
    val replaceRules = rules.filter { it.action != RegexAction.IGNORE.name }

    MenuScaffold(stringResource(R.string.nav_filters), scrollStore, "filters", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_when)) {
            MenuBody { Text(stringResource(R.string.filters_help), style = MaterialTheme.typography.bodyMedium) }
            MenuToggle(stringResource(R.string.filters_ignore_empty), policy.ignoreEmpty, {
                onPolicy(policy.copy(ignoreEmpty = it))
            })
            MenuToggle(stringResource(R.string.filters_ignore_group), policy.ignoreGroup, {
                onPolicy(policy.copy(ignoreGroup = it))
            }, true)
            MenuToggle(stringResource(R.string.filters_ignore_repeats), policy.ignoreRepeats, {
                onPolicy(policy.copy(ignoreRepeats = it))
            }, true)
            MenuToggle(stringResource(R.string.filters_collapse_repeats), policy.collapseRepeats, {
                onPolicy(policy.copy(collapseRepeats = it))
            }, true)
            MenuToggle(stringResource(R.string.filters_dnd_priority), policy.dndPriorityOnly, {
                onPolicy(policy.copy(dndPriorityOnly = it))
            }, true)
            MenuBody { Text(stringResource(R.string.filters_min_importance), style = MaterialTheme.typography.titleMedium) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                SpeakImportance.entries.forEach { level ->
                    FilterChip(
                        selected = policy.minImportance == level,
                        onClick = { onPolicy(policy.copy(minImportance = level)) },
                        label = { Text(stringResource(importanceLabel(level))) },
                    )
                }
            }
        }
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuBody {
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
                ) { Text(stringResource(R.string.filters_add)) }
            }
        }
        RuleList(stringResource(R.string.filters_action_ignore), ignoreRules, onDelete)
        RuleList(stringResource(R.string.filters_action_replace), replaceRules, onDelete)
    }
}

private fun importanceLabel(level: SpeakImportance): Int = when (level) {
    SpeakImportance.ANY -> R.string.filters_importance_any
    SpeakImportance.LOW -> R.string.filters_importance_low
    SpeakImportance.DEFAULT -> R.string.filters_importance_default
    SpeakImportance.HIGH -> R.string.filters_importance_high
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
    MenuSection(title) {
        rules.forEachIndexed { index, rule ->
            MenuLink(
                stringResource(R.string.filters_item, rule.action, rule.pattern),
                { onDelete(rule.id) },
                stringResource(R.string.filters_delete),
                showDivider = index > 0,
            )
        }
    }
}
