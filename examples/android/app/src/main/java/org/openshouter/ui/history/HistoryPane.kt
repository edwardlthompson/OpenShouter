package org.openshouter.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.openshouter.data.HistoryEntity
import org.openshouter.domain.AppSpeakRule
import org.openshouter.domain.HistorySpeak
import org.openshouter.notification.NotificationChannelSettings
import org.openshouter.service.OpenShouterEntryPoint
import org.openshouter.ui.menu.MenuScrollStore

@Composable
fun HistoryPane(
    rows: List<HistoryEntity>,
    showSpoken: Boolean,
    onShowSpoken: (Boolean) -> Unit,
    appRules: List<AppSpeakRule>,
    ep: OpenShouterEntryPoint,
    scope: CoroutineScope,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val byPackage = remember(appRules) { appRules.associateBy { it.packageName } }
    HistoryScreen(
        rows = rows,
        showSpoken = showSpoken,
        onShowSpoken = onShowSpoken,
        shouted = { pkg -> HistorySpeak.isShouting(pkg, byPackage) },
        onShoutChange = { pkg, on ->
            val flags = HistorySpeak.enabledFlags(on)
            scope.launch { ep.appSpeak().set(pkg, flags.first, flags.second) }
        },
        onOpenChannelSettings = { pkg, channelId ->
            NotificationChannelSettings.launch(context, pkg, channelId)
        },
        onClear = { scope.launch { ep.history().clear() } },
        onBack = onBack,
        scrollStore = scrollStore,
        modifier = modifier,
    )
}
