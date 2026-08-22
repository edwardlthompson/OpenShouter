package org.openshouter.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import kotlinx.coroutines.launch
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection

@Composable
fun BackupScreen(
    onExportBytes: () -> ByteArray,
    onImportBytes: suspend (ByteArray) -> Int,
    onImportInstalled: suspend () -> Int,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    val done = stringResource(R.string.backup_legacy_done)
    val none = stringResource(R.string.backup_legacy_none)
    fun report(count: Int) {
        status = if (count > 0) done.format(count) else none
    }
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupSaf.MIME_ZIP)) { uri ->
        if (uri != null) BackupSaf.write(context, uri, onExportBytes())
    }
    val openZip = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) BackupSaf.read(context, uri)?.let { bytes -> scope.launch { report(onImportBytes(bytes)) } }
    }
    val openDb = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) BackupSaf.read(context, uri)?.let { bytes -> scope.launch { report(onImportBytes(bytes)) } }
    }
    MenuScaffold(stringResource(R.string.nav_backup), scrollStore, "backup", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuBody { Text(stringResource(R.string.backup_help)) }
            MenuBody { Text(stringResource(R.string.backup_legacy_help)) }
            MenuBody { Text(stringResource(R.string.backup_legacy_steps)) }
            if (status.isNotEmpty()) MenuBody { Text(status) }
            MenuLink(stringResource(R.string.backup_export), { create.launch(BackupSaf.DEFAULT_NAME) }, showDivider = true)
            MenuLink(stringResource(R.string.backup_import), { openZip.launch(arrayOf(BackupSaf.MIME_ZIP)) }, showDivider = true)
            MenuLink(stringResource(R.string.backup_legacy), { scope.launch { report(onImportInstalled()) } }, showDivider = true)
            MenuLink(stringResource(R.string.backup_legacy_file), { openDb.launch(arrayOf("*/*")) }, showDivider = true)
        }
    }
}
