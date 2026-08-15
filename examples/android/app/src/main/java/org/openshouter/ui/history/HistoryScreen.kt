package org.openshouter.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import java.text.DateFormat
import java.util.Date
import org.openshouter.data.HistoryEntity

@Composable
fun HistoryScreen(
    rows: List<HistoryEntity>,
    showSpoken: Boolean,
    onShowSpoken: (Boolean) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormat = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    }
    val showSpokenLabel = stringResource(R.string.history_show_spoken)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.nav_history), style = MaterialTheme.typography.headlineSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(showSpokenLabel, modifier = Modifier.weight(1f))
            Switch(
                checked = showSpoken,
                onCheckedChange = onShowSpoken,
                modifier = Modifier.semantics { contentDescription = showSpokenLabel },
            )
        }
        if (rows.isEmpty()) {
            Text(stringResource(R.string.history_empty))
        } else {
            rows.forEach { row ->
                val formattedTime = timeFormat.format(Date(row.postedAt))
                Text(stringResource(R.string.history_row, row.packageName, formattedTime))
                if (row.ignoreReason.isNotBlank() && row.ignoreReason != "NONE") {
                    Text(
                        stringResource(R.string.history_reason, row.ignoreReason),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (showSpoken) {
                    Text(row.spoken, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Button(onClick = onClear) {
            Text(stringResource(R.string.history_clear))
        }
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}
