package org.openshouter.ui.astro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.foss.goldenpath.R
import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.LunarEventType
import org.openshouter.astro.model.SolarEventType
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TargetTypeSelector(
    currentTarget: AlarmTarget,
    onTargetChange: (AlarmTarget) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentTarget is AlarmTarget.Solar,
            onClick = {
                if (currentTarget !is AlarmTarget.Solar) {
                    onTargetChange(AlarmTarget.Solar(SolarEventType.Sunrise, 0))
                }
            },
            label = { Text(stringResource(R.string.astro_tab_sun)) }
        )
        FilterChip(
            selected = currentTarget is AlarmTarget.Lunar,
            onClick = {
                if (currentTarget !is AlarmTarget.Lunar) {
                    onTargetChange(AlarmTarget.Lunar(LunarEventType.FullMoon, 0))
                }
            },
            label = { Text(stringResource(R.string.astro_tab_moon)) }
        )
        FilterChip(
            selected = currentTarget is AlarmTarget.CustomClock,
            onClick = {
                if (currentTarget !is AlarmTarget.CustomClock) {
                    onTargetChange(AlarmTarget.CustomClock(7, 0))
                }
            },
            label = { Text(stringResource(R.string.astro_tab_clock)) }
        )
    }
}

@Composable
fun SolarEventPicker(
    selectedEvent: SolarEventType,
    onSelectEvent: (SolarEventType) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.astro_solar_event_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = AstroEventLabels.solarLabel(selectedEvent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = AstroEventLabels.solarDescription(selectedEvent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.astro_action_change),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }

    if (showDialog) {
        SolarEventListDialog(
            current = selectedEvent,
            onSelect = {
                onSelectEvent(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun LunarEventPicker(
    selectedEvent: LunarEventType,
    onSelectEvent: (LunarEventType) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.astro_lunar_event_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = AstroEventLabels.lunarLabel(selectedEvent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = AstroEventLabels.lunarDescription(selectedEvent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.astro_action_change),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }

    if (showDialog) {
        LunarEventListDialog(
            current = selectedEvent,
            onSelect = {
                onSelectEvent(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OffsetSelector(
    offsetMinutes: Int,
    eventName: String,
    onOffsetChange: (Int) -> Unit
) {
    val totalAbsMinutes = abs(offsetMinutes)
    val hours = totalAbsMinutes / 60
    val minutes = totalAbsMinutes % 60
    val isExact = offsetMinutes == 0
    val isBefore = offsetMinutes < 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = AstroEventLabels.offsetSummary(offsetMinutes, eventName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isBefore,
                    onClick = {
                        val currentAbs = if (totalAbsMinutes == 0) 15 else totalAbsMinutes
                        onOffsetChange(-currentAbs)
                    },
                    label = { Text(stringResource(R.string.astro_offset_direction_before)) }
                )
                FilterChip(
                    selected = isExact,
                    onClick = { onOffsetChange(0) },
                    label = { Text(stringResource(R.string.astro_offset_direction_exact)) }
                )
                FilterChip(
                    selected = !isBefore && !isExact,
                    onClick = {
                        val currentAbs = if (totalAbsMinutes == 0) 15 else totalAbsMinutes
                        onOffsetChange(currentAbs)
                    },
                    label = { Text(stringResource(R.string.astro_offset_direction_after)) }
                )
            }

            if (!isExact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AstroNumberWheel(
                        value = hours,
                        range = (0..12).toList(),
                        label = stringResource(R.string.astro_offset_hours),
                        onValueChange = { newHours ->
                            val sign = if (isBefore) -1 else 1
                            onOffsetChange(sign * (newHours * 60 + minutes))
                        }
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    AstroNumberWheel(
                        value = minutes,
                        range = (0..59).toList(),
                        label = stringResource(R.string.astro_offset_minutes),
                        onValueChange = { newMinutes ->
                            val sign = if (isBefore) -1 else 1
                            onOffsetChange(sign * (hours * 60 + newMinutes))
                        }
                    )
                }
            }
        }
    }
}
