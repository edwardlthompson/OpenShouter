package org.openshouter.ui.quiet

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.AppSettings
import org.openshouter.domain.QuietHours

private val DAY_LABELS = intArrayOf(
    R.string.quiet_day_1,
    R.string.quiet_day_2,
    R.string.quiet_day_3,
    R.string.quiet_day_4,
    R.string.quiet_day_5,
    R.string.quiet_day_6,
    R.string.quiet_day_7,
)

private const val NIGHT_START = 22 * 60
private const val NIGHT_END = 7 * 60

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuietHoursScreen(
    settings: AppSettings,
    onChange: (Boolean, Int, Int, Set<Int>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickingStart by remember { mutableStateOf(true) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.quiet_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.quiet_help), style = MaterialTheme.typography.bodyMedium)
        val enableLabel = stringResource(
            R.string.announcer_quiet,
            QuietHours.windowLabel(settings.quietStartMinutes, settings.quietEndMinutes),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(enableLabel, modifier = Modifier.weight(1f))
            Switch(
                checked = settings.quietHoursEnabled,
                onCheckedChange = {
                    onChange(it, settings.quietStartMinutes, settings.quietEndMinutes, settings.quietDays)
                },
                modifier = Modifier.semantics { contentDescription = enableLabel },
            )
        }
        TimeRow(
            label = stringResource(R.string.quiet_start),
            minutes = settings.quietStartMinutes,
            onNudge = { delta ->
                onChange(
                    settings.quietHoursEnabled,
                    QuietHours.nudge(settings.quietStartMinutes, delta),
                    settings.quietEndMinutes,
                    settings.quietDays,
                )
            },
        )
        TimeRow(
            label = stringResource(R.string.quiet_end),
            minutes = settings.quietEndMinutes,
            onNudge = { delta ->
                onChange(
                    settings.quietHoursEnabled,
                    settings.quietStartMinutes,
                    QuietHours.nudge(settings.quietEndMinutes, delta),
                    settings.quietDays,
                )
            },
        )
        HourGrid(
            startMinutes = settings.quietStartMinutes,
            endMinutes = settings.quietEndMinutes,
            pickingStart = pickingStart,
            onHour = { hour ->
                val minutes = hour * 60
                if (pickingStart) {
                    onChange(settings.quietHoursEnabled, minutes, settings.quietEndMinutes, settings.quietDays)
                    pickingStart = false
                } else {
                    onChange(settings.quietHoursEnabled, settings.quietStartMinutes, minutes, settings.quietDays)
                    pickingStart = true
                }
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Button(
                onClick = {
                    onChange(settings.quietHoursEnabled, NIGHT_START, NIGHT_END, QuietHours.ALL_DAYS)
                    pickingStart = true
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.quiet_reset))
            }
            Button(
                onClick = {
                    onChange(settings.quietHoursEnabled, NIGHT_START, NIGHT_END, QuietHours.ALL_DAYS)
                    pickingStart = true
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.quiet_preset))
            }
        }
        Text(stringResource(R.string.quiet_days), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            QuietHours.ALL_DAYS.forEach { day ->
                val label = stringResource(DAY_LABELS[day - 1])
                FilterChip(
                    selected = day in settings.quietDays,
                    onClick = {
                        onChange(
                            settings.quietHoursEnabled,
                            settings.quietStartMinutes,
                            settings.quietEndMinutes,
                            QuietHours.toggleDay(settings.quietDays, day),
                        )
                    },
                    label = { Text(label) },
                )
            }
        }
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HourGrid(
    startMinutes: Int,
    endMinutes: Int,
    pickingStart: Boolean,
    onHour: (Int) -> Unit,
) {
    Text(
        stringResource(if (pickingStart) R.string.quiet_start else R.string.quiet_end),
        style = MaterialTheme.typography.titleMedium,
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        (0 until 24).forEach { hour ->
            val clock = QuietHours.clockLabel(hour * 60)
            FilterChip(
                selected = hourOverlapsWindow(hour, startMinutes, endMinutes),
                onClick = { onHour(hour) },
                label = { Text(hour.toString()) },
                modifier = Modifier.semantics { contentDescription = clock },
            )
        }
    }
}

@Composable
private fun TimeRow(label: String, minutes: Int, onNudge: (Int) -> Unit) {
    val earlier = stringResource(R.string.quiet_earlier, label)
    val later = stringResource(R.string.quiet_later, label)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Button(onClick = { onNudge(-1) }, modifier = Modifier.semantics { contentDescription = earlier }) {
            Text(stringResource(R.string.quiet_minus))
        }
        Text(QuietHours.clockLabel(minutes), style = MaterialTheme.typography.titleMedium)
        Button(onClick = { onNudge(1) }, modifier = Modifier.semantics { contentDescription = later }) {
            Text(stringResource(R.string.quiet_plus))
        }
    }
}

private fun hourOverlapsWindow(hour: Int, start: Int, end: Int): Boolean {
    if (start == end) return false
    val hourStart = hour * 60
    val hourEnd = hourStart + 60
    return if (start < end) {
        hourStart < end && hourEnd > start
    } else {
        hourEnd > start || hourStart < end
    }
}
