package org.openshouter.time

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.domain.AppSettings
import org.openshouter.domain.TimeHourStyle
import org.openshouter.domain.TimeShout
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuDropdown
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

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
    val intervals = listOf(
        TimeShout.INTERVAL_QUARTER to stringResource(R.string.time_15),
        TimeShout.INTERVAL_HALF to stringResource(R.string.time_30),
        TimeShout.INTERVAL_HOUR to stringResource(R.string.time_60),
    )
    val hours = listOf(
        TimeHourStyle.HOUR_12 to stringResource(R.string.time_hour_12),
        TimeHourStyle.HOUR_24 to stringResource(R.string.time_hour_24),
        TimeHourStyle.SYSTEM to stringResource(R.string.time_hour_system),
    )
    MenuScaffold(stringResource(R.string.time_title), scrollStore, "time", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            MenuToggle(stringResource(R.string.announcer_time), settings.timeShoutEnabled, {
                onChange(it, settings.timeShoutIntervalMinutes, settings.timeShoutExact)
            })
            MenuToggle(stringResource(R.string.time_exact), settings.timeShoutExact, {
                onChange(settings.timeShoutEnabled, settings.timeShoutIntervalMinutes, it)
            }, true)
            MenuBody {
                MenuDropdown(
                    label = stringResource(R.string.time_interval),
                    text = intervals.firstOrNull { it.first == settings.timeShoutIntervalMinutes }?.second
                        ?: intervals.last().second,
                    options = intervals.map { it.first.toString() to it.second },
                    onSelect = { raw ->
                        val minutes = raw.toIntOrNull() ?: TimeShout.INTERVAL_HOUR
                        onChange(settings.timeShoutEnabled, minutes, settings.timeShoutExact)
                    },
                )
                MenuDropdown(
                    label = stringResource(R.string.time_hour_style),
                    text = hours.firstOrNull { it.first == settings.timeHourStyle }?.second ?: hours.last().second,
                    options = hours.map { it.first.name to it.second },
                    onSelect = { name ->
                        val style = runCatching { TimeHourStyle.valueOf(name) }.getOrDefault(TimeHourStyle.SYSTEM)
                        onHourStyle(style)
                    },
                )
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
