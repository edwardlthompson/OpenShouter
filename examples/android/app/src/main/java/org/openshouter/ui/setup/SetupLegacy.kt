package org.openshouter.ui.setup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import kotlinx.coroutines.launch
import org.openshouter.backup.BackupSaf
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuSection

@Composable
fun SetupLegacyImport(
    onImportInstalled: suspend () -> Int,
    onImportBytes: suspend (ByteArray) -> Int,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    val done = stringResource(R.string.backup_legacy_done)
    val none = stringResource(R.string.backup_legacy_none)
    fun report(count: Int) {
        status = if (count > 0) done.format(count) else none
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) BackupSaf.read(context, uri)?.let { bytes -> scope.launch { report(onImportBytes(bytes)) } }
    }
    MenuSection(stringResource(R.string.setup_legacy_section)) {
        MenuBody { Text(stringResource(R.string.backup_legacy_help)) }
        MenuBody { Text(stringResource(R.string.backup_legacy_steps)) }
        if (status.isNotEmpty()) MenuBody { Text(status) }
        MenuLink(stringResource(R.string.backup_legacy), { scope.launch { report(onImportInstalled()) } }, showDivider = true)
        MenuLink(stringResource(R.string.backup_legacy_file), { open.launch(arrayOf("*/*")) }, showDivider = true)
    }
}
