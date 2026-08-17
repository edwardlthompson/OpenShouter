package org.openshouter.oem

import android.content.Intent
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
fun OemScreen(
    onOpen: (Intent) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    MenuScaffold(stringResource(R.string.oem_title), scrollStore, "oem", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuBody { Text(stringResource(R.string.oem_help)) }
            MenuLink(
                stringResource(R.string.oem_open),
                { OemAutostart.settingsIntent(context)?.let(onOpen) },
                showDivider = true,
            )
        }
    }
}
