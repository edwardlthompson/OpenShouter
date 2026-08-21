package org.openshouter.ui.updates

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R

@Composable
fun DonateNudgeDialog(
    onDonate: () -> Unit,
    onLater: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onLater,
        title = { Text(stringResource(R.string.about_donate_nudge_title)) },
        text = { Text(stringResource(R.string.about_donate_nudge_message)) },
        confirmButton = {
            TextButton(onClick = onDonate) {
                Text(stringResource(R.string.about_donate))
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text(stringResource(R.string.about_not_now))
            }
        },
    )
}

@Composable
fun UpdateAvailableDialog(
    version: String,
    onInstall: () -> Unit,
    onLater: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onLater,
        title = { Text(stringResource(R.string.about_update_prompt_title)) },
        text = { Text(stringResource(R.string.about_update_prompt_message, version)) },
        confirmButton = {
            TextButton(onClick = onInstall) {
                Text(stringResource(R.string.about_install))
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text(stringResource(R.string.about_later))
            }
        },
    )
}
