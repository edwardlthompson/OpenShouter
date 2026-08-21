package org.openshouter.ui.history

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import java.text.DateFormat
import java.util.Date
import org.openshouter.data.HistoryEntity
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
    onClear: () -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val timeFormat = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    }
    MenuScaffold(stringResource(R.string.nav_history), scrollStore, "history", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuToggle(stringResource(R.string.history_show_spoken), showSpoken, onShowSpoken)
            MenuLink(stringResource(R.string.history_clear), onClear, showDivider = true)
        }
        MenuSection(stringResource(R.string.menu_section_list)) {
            if (rows.isEmpty()) {
                MenuBody { Text(stringResource(R.string.history_empty)) }
            } else {
                MenuBody {
                    rows.forEach { row ->
                        val formattedTime = timeFormat.format(Date(row.postedAt))
                        Text(stringResource(R.string.history_row, row.packageName, formattedTime))
                        val reasonRes = ignoreReasonLabel(row.ignoreReason)
                        if (reasonRes != null) {
                            Text(
                                stringResource(R.string.history_reason, stringResource(reasonRes)),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        if (showSpoken) {
                            Text(row.spoken, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
