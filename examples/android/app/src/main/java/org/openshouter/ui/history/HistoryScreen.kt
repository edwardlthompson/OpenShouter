package org.openshouter.ui.history

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import java.text.DateFormat
import java.util.Date
import org.openshouter.call.CallNotification
import org.openshouter.data.HistoryEntity
import org.openshouter.domain.CallRepeatMode
import org.openshouter.domain.CallRepeatModes
import org.openshouter.domain.ShoutHistory
import org.openshouter.domain.SpokenEvent
import org.openshouter.message.MessageChannel
import org.openshouter.notification.NotificationFacts
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@Composable
fun HistoryScreen(
    rows: List<HistoryEntity>,
    showSpoken: Boolean,
    onShowSpoken: (Boolean) -> Unit,
    shouted: (String) -> Boolean,
    onShoutChange: (String, Boolean) -> Unit,
    callRepeatModes: Map<String, CallRepeatMode>,
    onCallRepeatChange: (String, CallRepeatMode) -> Unit,
    onOpenChannelSettings: (String, String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
    onSpeakRow: ((String) -> Unit)? = null,
) {
    val timeFormat = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    }
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<HistoryEntity?>(null) }
    val filteredRows = remember(rows, query) {
        if (query.isBlank()) rows
        else rows.filter {
            it.packageName.contains(query, ignoreCase = true) ||
                it.title.contains(query, ignoreCase = true) ||
                it.spoken.contains(query, ignoreCase = true)
        }
    }
    MenuScaffold(stringResource(R.string.nav_history), scrollStore, "history", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuBody {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.history_search)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            MenuToggle(stringResource(R.string.history_show_spoken), showSpoken, onShowSpoken)
            MenuLink(stringResource(R.string.history_clear), onClear, showDivider = true)
        }
        MenuSection(stringResource(R.string.menu_section_list)) {
            if (filteredRows.isEmpty()) {
                MenuBody { Text(stringResource(R.string.history_empty)) }
            } else {
                filteredRows.forEachIndexed { index, row ->
                    val formattedTime = timeFormat.format(Date(row.postedAt))
                    val reasonRes = ignoreReasonLabel(row.ignoreReason)
                    val reason = reasonRes?.let {
                        stringResource(R.string.history_reason, stringResource(it))
                    }
                    val supporting = listOfNotNull(
                        reason,
                        row.spoken.takeIf { showSpoken && it.isNotBlank() },
                    ).joinToString("\n").ifBlank { null }
                    val sourceRes = historySourceLabel(row.kind)
                    val headline = sourceRes?.let { stringResource(it) } ?: row.packageName
                    MenuLink(
                        stringResource(R.string.history_row, headline, formattedTime),
                        { selected = row },
                        supporting = supporting,
                        showDivider = index > 0,
                    )
                }
            }
        }
    }
    val row = selected
    if (row != null) {
        val internal = ShoutHistory.isInternalKind(row.kind)
        val sourceRes = historySourceLabel(row.kind)
        val packageLabel = remember(row.packageName) {
            NotificationFacts.label(context.packageManager, row.packageName)
        }
        val appLabel = if (internal && sourceRes != null) {
            stringResource(sourceRes)
        } else {
            packageLabel
        }
        val channelLabel = row.channelName.ifBlank {
            stringResource(R.string.history_channel_settings)
        }
        val cellular = !internal && row.kind == SpokenEvent.Kind.CALL.name &&
            CallNotification.isCellularDialer(row.packageName)
        val showCallRepeat = !internal && !cellular && (
            MessageChannel.isMessaging(row.packageName) || row.kind == SpokenEvent.Kind.CALL.name
        )
        HistoryMuteDialog(
            appLabel = appLabel,
            shoutEnabled = shouted(row.packageName),
            onShoutChange = { on -> onShoutChange(row.packageName, on) },
            channelLabel = channelLabel,
            onOpenChannelSettings = { onOpenChannelSettings(row.packageName, row.channelId) },
            onDismiss = { selected = null },
            showShoutToggle = !internal && !cellular,
            showChannelToggle = !internal,
            callRepeat = if (showCallRepeat) {
                CallRepeatModes.modeFor(row.packageName, callRepeatModes)
            } else {
                null
            },
            onCallRepeatChange = { mode -> onCallRepeatChange(row.packageName, mode) },
            cellularRepeats = cellular,
            spokenText = row.spoken,
            onSpeakRow = onSpeakRow,
        )
    }
}
