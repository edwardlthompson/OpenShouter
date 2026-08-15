package org.openshouter.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.data.ReminderEntity
import org.openshouter.domain.ReminderInterval

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderScreen(
    reminders: List<ReminderEntity>,
    onAdd: (String, Boolean, Int) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    var alsoNotify by remember { mutableStateOf(false) }
    var interval by remember { mutableIntStateOf(ReminderInterval.HOUR) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.nav_reminders), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.reminders_help), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.reminders_text)) },
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ReminderInterval.ALL.forEach { minutes ->
                FilterChip(
                    selected = interval == minutes,
                    onClick = { interval = minutes },
                    label = { Text(stringResource(intervalLabel(minutes))) },
                )
            }
        }
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.reminders_also))
            Switch(checked = alsoNotify, onCheckedChange = { alsoNotify = it })
        }
        Button(
            onClick = {
                val normalized = ReminderEntity.normalizeText(text) ?: return@Button
                onAdd(normalized, alsoNotify, interval)
                text = ""
            },
        ) {
            Text(stringResource(R.string.reminders_add))
        }
        reminders.forEach { row ->
            Text(stringResource(R.string.reminders_item, row.text, row.intervalMinutes))
            Button(onClick = { onDelete(row.id) }) {
                Text(stringResource(R.string.reminders_delete))
            }
        }
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}

fun reminderDefaults(
    text: String,
    nowMillis: Long,
    alsoNotify: Boolean = false,
    intervalMinutes: Int = ReminderInterval.HOUR,
): ReminderEntity? {
    val normalized = ReminderEntity.normalizeText(text) ?: return null
    val interval = ReminderInterval.normalize(intervalMinutes)
    return ReminderEntity(
        text = normalized,
        intervalMinutes = interval,
        nextAtMillis = ReminderInterval.nextAt(nowMillis, interval),
        enabled = true,
        alsoNotify = alsoNotify,
    )
}

private fun intervalLabel(minutes: Int): Int = when (minutes) {
    ReminderInterval.HOUR -> R.string.reminders_hour
    ReminderInterval.DAY -> R.string.reminders_day
    ReminderInterval.WEEK -> R.string.reminders_week
    ReminderInterval.MONTH -> R.string.reminders_month
    else -> R.string.reminders_year
}
