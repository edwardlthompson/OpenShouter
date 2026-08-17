package org.openshouter.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection

@Composable
fun BackupScreen(
    onExportBytes: () -> ByteArray,
    onImportBytes: (ByteArray) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupSaf.MIME_ZIP)) { uri ->
        if (uri != null) BackupSaf.write(context, uri, onExportBytes())
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) BackupSaf.read(context, uri)?.let(onImportBytes)
    }
    MenuScaffold(stringResource(R.string.nav_backup), scrollStore, "backup", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuBody { Text(stringResource(R.string.backup_help)) }
            MenuLink(stringResource(R.string.backup_export), { create.launch(BackupSaf.DEFAULT_NAME) }, showDivider = true)
            MenuLink(stringResource(R.string.backup_import), { open.launch(arrayOf(BackupSaf.MIME_ZIP)) }, showDivider = true)
        }
    }
}
