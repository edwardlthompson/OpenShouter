package org.openshouter.ui.filters

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
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
import org.openshouter.data.RegexEntity
import org.openshouter.domain.NotificationPolicy
import org.openshouter.domain.RegexAction
import org.openshouter.domain.RegexFilter
import org.openshouter.domain.SpeakImportance
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuDropdown
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

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
            MenuToggle(stringResource(R.string.filters_ignore_bubbles), policy.ignoreBubbles, {
                onPolicy(policy.copy(ignoreBubbles = it))
            }, true)
            MenuToggle(stringResource(R.string.filters_ignore_work_profile), policy.ignoreWorkProfile, {
                onPolicy(policy.copy(ignoreWorkProfile = it))
            }, true)
            MenuToggle(stringResource(R.string.filters_collapse_repeats), policy.collapseRepeats, {
                onPolicy(policy.copy(collapseRepeats = it))
            }, true)
            MenuToggle(stringResource(R.string.filters_dnd_priority), policy.dndPriorityOnly, {
                onPolicy(policy.copy(dndPriorityOnly = it))
            }, true)
            MenuBody {
                val levels = SpeakImportance.entries.map { it.name to stringResource(importanceLabel(it)) }
                MenuDropdown(
                    label = stringResource(R.string.filters_min_importance),
                    text = levels.firstOrNull { it.first == policy.minImportance.name }?.second ?: levels.first().second,
                    options = levels,
                    onSelect = { name ->
                        val level = runCatching { SpeakImportance.valueOf(name) }.getOrDefault(SpeakImportance.ANY)
                        onPolicy(policy.copy(minImportance = level))
                    },
                )
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
                val actions = listOf(
                    RegexAction.IGNORE to stringResource(R.string.filters_action_ignore),
                    RegexAction.REPLACE to stringResource(R.string.filters_action_replace),
                )
                MenuDropdown(
                    label = stringResource(R.string.menu_section_actions),
                    text = actions.firstOrNull { it.first == action }?.second ?: actions.first().second,
                    options = actions.map { it.first.name to it.second },
                    onSelect = { name ->
                        action = runCatching { RegexAction.valueOf(name) }.getOrDefault(RegexAction.IGNORE)
                    },
                )
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
