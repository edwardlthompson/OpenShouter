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
import org.openshouter.ui.menu.MenuToggle

@Composable
fun HistoryMuteDialog(
    appLabel: String,
    shoutEnabled: Boolean,
    onShoutChange: (Boolean) -> Unit,
    channelLabel: String,
    onOpenChannelSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    var channelOn by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_manage_title, appLabel)) },
        text = {
            Column {
                MenuToggle(
                    stringResource(R.string.history_toggle_openshouter),
                    shoutEnabled,
                    onShoutChange,
                )
                MenuToggle(
                    channelLabel,
                    channelOn,
                    { on ->
                        channelOn = on
                        onOpenChannelSettings()
                    },
                    showDivider = true,
                )
                Text(
                    stringResource(R.string.history_channel_settings_help),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = SpacingMd),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.history_dialog_close))
            }
        },
    )
}
