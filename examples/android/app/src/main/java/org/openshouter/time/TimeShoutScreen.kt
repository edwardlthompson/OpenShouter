package org.openshouter.time

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.AppSettings
import org.openshouter.domain.TimeHourStyle
import org.openshouter.domain.TimeShout

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeShoutScreen(
    settings: AppSettings,
    onChange: (Boolean, Int, Boolean) -> Unit,
    onFormat: (String) -> Unit = {},
    onHourStyle: (TimeHourStyle) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enableLabel = stringResource(R.string.announcer_time)
    val exactLabel = stringResource(R.string.time_exact)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.time_title), style = MaterialTheme.typography.headlineSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(enableLabel, modifier = Modifier.weight(1f))
            Switch(
                checked = settings.timeShoutEnabled,
                onCheckedChange = {
                    onChange(it, settings.timeShoutIntervalMinutes, settings.timeShoutExact)
                },
                modifier = Modifier.semantics { contentDescription = enableLabel },
            )
        }
        Text(stringResource(R.string.time_interval), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            IntervalChip(TimeShout.INTERVAL_QUARTER, R.string.time_15, settings, onChange)
            IntervalChip(TimeShout.INTERVAL_HALF, R.string.time_30, settings, onChange)
            IntervalChip(TimeShout.INTERVAL_HOUR, R.string.time_60, settings, onChange)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(exactLabel, modifier = Modifier.weight(1f))
            Switch(
                checked = settings.timeShoutExact,
                onCheckedChange = {
                    onChange(settings.timeShoutEnabled, settings.timeShoutIntervalMinutes, it)
                },
                modifier = Modifier.semantics { contentDescription = exactLabel },
            )
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
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
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
