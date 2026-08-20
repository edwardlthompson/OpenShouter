package org.openshouter.bluetooth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@Composable
fun BluetoothShoutScreen(
    connectAlert: Boolean,
    batteryAlert: Boolean,
    onConnect: (Boolean) -> Unit,
    onBattery: (Boolean) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    MenuScaffold(stringResource(R.string.nav_bluetooth), scrollStore, "bluetooth", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            MenuToggle(stringResource(R.string.bluetooth_connect), connectAlert, onConnect)
            MenuToggle(stringResource(R.string.bluetooth_battery), batteryAlert, onBattery, true)
            MenuBody {
                Text(stringResource(R.string.bluetooth_help), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
