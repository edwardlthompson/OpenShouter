package org.openshouter.ui.overrides

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
import org.openshouter.domain.AppOverride
import org.openshouter.domain.TtsStream
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuDropdown
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection

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
                val inherit = stringResource(R.string.overrides_inherit)
                val streams = listOf("" to inherit) + listOf(
                    TtsStream.NOTIFICATION to stringResource(R.string.tts_stream_notification),
                    TtsStream.MEDIA to stringResource(R.string.tts_stream_media),
                    TtsStream.ALARM to stringResource(R.string.tts_stream_alarm),
                ).map { it.first.name to it.second }
                MenuDropdown(
                    label = stringResource(R.string.overrides_stream),
                    text = streams.firstOrNull { it.first == (stream?.name.orEmpty()) }?.second ?: inherit,
                    options = streams,
                    onSelect = { name ->
                        stream = name.takeIf { it.isNotEmpty() }?.let { runCatching { TtsStream.valueOf(it) }.getOrNull() }
                    },
                )
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

@Composable
private fun InheritRow(label: String, value: Boolean?, onChange: (Boolean?) -> Unit) {
    val inherit = stringResource(R.string.overrides_inherit)
    val yes = stringResource(R.string.overrides_yes)
    val no = stringResource(R.string.overrides_no)
    val options = listOf("inherit" to inherit, "yes" to yes, "no" to no)
    val text = when (value) {
        true -> yes
        false -> no
        null -> inherit
    }
    MenuDropdown(
        label = label,
        text = text,
        options = options,
        onSelect = { id ->
            onChange(
                when (id) {
                    "yes" -> true
                    "no" -> false
                    else -> null
                },
            )
        },
    )
}
