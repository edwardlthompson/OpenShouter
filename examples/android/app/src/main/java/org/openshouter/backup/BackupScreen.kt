package org.openshouter.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun BackupScreen(
    onExportBytes: () -> ByteArray,
    onImportBytes: (ByteArray) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupSaf.MIME_ZIP)) { uri ->
        if (uri != null) BackupSaf.write(context, uri, onExportBytes())
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) BackupSaf.read(context, uri)?.let(onImportBytes)
    }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.nav_backup), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.backup_help), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { create.launch(BackupSaf.DEFAULT_NAME) }) {
            Text(stringResource(R.string.backup_export))
        }
        Button(onClick = { open.launch(arrayOf(BackupSaf.MIME_ZIP)) }) {
            Text(stringResource(R.string.backup_import))
        }
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}
