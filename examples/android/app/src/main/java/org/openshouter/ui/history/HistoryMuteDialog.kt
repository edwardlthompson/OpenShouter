package org.openshouter.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.CallRepeatMode
import org.openshouter.ui.call.CallRepeatDropdown
import org.openshouter.ui.menu.MenuToggle

@Composable
fun HistoryMuteDialog(
    appLabel: String,
    shoutEnabled: Boolean,
    onShoutChange: (Boolean) -> Unit,
    channelLabel: String,
    onOpenChannelSettings: () -> Unit,
    onDismiss: () -> Unit,
    showShoutToggle: Boolean = true,
    showChannelToggle: Boolean = true,
    callRepeat: CallRepeatMode? = null,
    onCallRepeatChange: (CallRepeatMode) -> Unit = {},
    cellularRepeats: Boolean = false,
    spokenText: String? = null,
    onSpeakRow: ((String) -> Unit)? = null,
) {
    var channelOn by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_manage_title, appLabel)) },
        text = {
            Column {
                if (!spokenText.isNullOrBlank() && onSpeakRow != null) {
                    TextButton(onClick = { onSpeakRow(spokenText) }) {
                        Text(stringResource(R.string.history_speak_row))
                    }
                }
                if (showShoutToggle) {
                    MenuToggle(
                        stringResource(R.string.history_toggle_openshouter),
                        shoutEnabled,
                        onShoutChange,
                    )
                }
                if (cellularRepeats) {
                    Text(
                        stringResource(R.string.history_call_cellular_repeats),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = SpacingMd, vertical = SpacingMd),
                    )
                }
                if (callRepeat != null) {
                    CallRepeatDropdown(mode = callRepeat, onChange = onCallRepeatChange)
                }
                if (showChannelToggle) {
                    MenuToggle(
                        channelLabel,
                        channelOn,
                        { on ->
                            channelOn = on
                            onOpenChannelSettings()
                        },
                        showDivider = showShoutToggle || cellularRepeats || callRepeat != null,
                    )
                    Text(
                        stringResource(R.string.history_channel_settings_help),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = SpacingMd),
                    )
                } else if (!showShoutToggle && !cellularRepeats && callRepeat == null) {
                    Text(
                        stringResource(R.string.history_internal_help),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = SpacingMd),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.history_dialog_close))
            }
        },
    )
}
