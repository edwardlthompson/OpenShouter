package org.openshouter.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
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
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.data.ReminderEntity
import org.openshouter.domain.ReminderInterval
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderScreen(
    reminders: List<ReminderEntity>,
    onAdd: (String, Boolean, Int) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    var alsoNotify by remember { mutableStateOf(false) }
    var interval by remember { mutableIntStateOf(ReminderInterval.HOUR) }
    MenuScaffold(stringResource(R.string.nav_reminders), scrollStore, "reminders", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuBody {
                Text(stringResource(R.string.reminders_help))
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
                Button(
                    onClick = {
                        val normalized = ReminderEntity.normalizeText(text) ?: return@Button
                        onAdd(normalized, alsoNotify, interval)
                        text = ""
                    },
                ) { Text(stringResource(R.string.reminders_add)) }
            }
            MenuToggle(stringResource(R.string.reminders_also), alsoNotify, { alsoNotify = it }, true)
        }
        if (reminders.isNotEmpty()) {
            MenuSection(stringResource(R.string.menu_section_list)) {
                reminders.forEachIndexed { index, row ->
                    MenuLink(
                        stringResource(R.string.reminders_item, row.text, row.intervalMinutes),
                        { onDelete(row.id) },
                        stringResource(R.string.reminders_delete),
                        showDivider = index > 0,
                    )
                }
            }
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
