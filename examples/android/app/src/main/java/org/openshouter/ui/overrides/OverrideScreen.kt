package org.openshouter.ui.overrides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import org.openshouter.domain.AppOverride
import org.openshouter.domain.TtsStream
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverrideScreen(
    overrides: Map<String, AppOverride>,
    onSave: (AppOverride) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    var pkg by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("") }
    var speakName by remember { mutableStateOf<Boolean?>(null) }
    var speakBody by remember { mutableStateOf<Boolean?>(null) }
    var ignoreEmpty by remember { mutableStateOf<Boolean?>(null) }
    var stream by remember { mutableStateOf<TtsStream?>(null) }
    MenuScaffold(stringResource(R.string.nav_overrides), scrollStore, "overrides", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuBody {
                Text(stringResource(R.string.overrides_help), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = pkg,
                    onValueChange = { pkg = it },
                    label = { Text(stringResource(R.string.overrides_package)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = format,
                    onValueChange = { format = it },
                    label = { Text(stringResource(R.string.overrides_format)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                InheritRow(stringResource(R.string.overrides_speak_name), speakName) { speakName = it }
                InheritRow(stringResource(R.string.overrides_speak_body), speakBody) { speakBody = it }
                InheritRow(stringResource(R.string.overrides_ignore_empty), ignoreEmpty) { ignoreEmpty = it }
                Text(stringResource(R.string.overrides_stream), style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    FilterChip(selected = stream == null, onClick = { stream = null }, label = { Text(stringResource(R.string.overrides_inherit)) })
                    TtsStream.entries.forEach { value ->
                        FilterChip(
                            selected = stream == value,
                            onClick = { stream = value },
                            label = { Text(value.name) },
                        )
                    }
                }
                Button(
                    onClick = {
                        if (pkg.isBlank()) return@Button
                        onSave(
                            AppOverride(
                                packageName = pkg.trim(),
                                format = format.trim().ifEmpty { null },
                                speakName = speakName,
                                speakBody = speakBody,
                                ignoreEmpty = ignoreEmpty,
                                stream = stream,
                            ),
                        )
                    },
                ) { Text(stringResource(R.string.overrides_save)) }
            }
        }
        if (overrides.isNotEmpty()) {
            MenuSection(stringResource(R.string.menu_section_list)) {
                MenuBody {
                    overrides.values.forEach { row ->
                        Text(
                            if (row.format.isNullOrBlank()) {
                                stringResource(R.string.overrides_item_inherit, row.packageName)
                            } else {
                                stringResource(R.string.overrides_item, row.packageName, row.format)
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InheritRow(label: String, value: Boolean?, onChange: (Boolean?) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FilterChip(selected = value == null, onClick = { onChange(null) }, label = { Text(stringResource(R.string.overrides_inherit)) })
            FilterChip(selected = value == true, onClick = { onChange(true) }, label = { Text(stringResource(R.string.overrides_yes)) })
            FilterChip(selected = value == false, onClick = { onChange(false) }, label = { Text(stringResource(R.string.overrides_no)) })
        }
    }
}
