package org.openshouter.time

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.AppSettings
import org.openshouter.domain.TimeHourStyle
import org.openshouter.domain.TimeShout
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeShoutScreen(
    settings: AppSettings,
    onChange: (Boolean, Int, Boolean) -> Unit,
    onFormat: (String) -> Unit = {},
    onHourStyle: (TimeHourStyle) -> Unit = {},
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    MenuScaffold(stringResource(R.string.time_title), scrollStore, "time", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            MenuToggle(stringResource(R.string.announcer_time), settings.timeShoutEnabled, {
                onChange(it, settings.timeShoutIntervalMinutes, settings.timeShoutExact)
            })
            MenuToggle(stringResource(R.string.time_exact), settings.timeShoutExact, {
                onChange(settings.timeShoutEnabled, settings.timeShoutIntervalMinutes, it)
            }, true)
            MenuBody {
                Text(stringResource(R.string.time_interval), style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    IntervalChip(TimeShout.INTERVAL_QUARTER, R.string.time_15, settings, onChange)
                    IntervalChip(TimeShout.INTERVAL_HALF, R.string.time_30, settings, onChange)
                    IntervalChip(TimeShout.INTERVAL_HOUR, R.string.time_60, settings, onChange)
                }
                Text(stringResource(R.string.time_hour_style), style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    HourChip(TimeHourStyle.HOUR_12, R.string.time_hour_12, settings, onHourStyle)
                    HourChip(TimeHourStyle.HOUR_24, R.string.time_hour_24, settings, onHourStyle)
                    HourChip(TimeHourStyle.SYSTEM, R.string.time_hour_system, settings, onHourStyle)
                }
                OutlinedTextField(
                    value = settings.timeFormat,
                    onValueChange = onFormat,
                    label = { Text(stringResource(R.string.time_phrase)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
    }
}

@Composable
private fun IntervalChip(
    minutes: Int,
    labelRes: Int,
    settings: AppSettings,
    onChange: (Boolean, Int, Boolean) -> Unit,
) {
    FilterChip(
        selected = settings.timeShoutIntervalMinutes == minutes,
        onClick = { onChange(settings.timeShoutEnabled, minutes, settings.timeShoutExact) },
        label = { Text(stringResource(labelRes)) },
    )
}

@Composable
private fun HourChip(
    style: TimeHourStyle,
    labelRes: Int,
    settings: AppSettings,
    onHourStyle: (TimeHourStyle) -> Unit,
) {
    FilterChip(
        selected = settings.timeHourStyle == style,
        onClick = { onHourStyle(style) },
        label = { Text(stringResource(labelRes)) },
    )
}
