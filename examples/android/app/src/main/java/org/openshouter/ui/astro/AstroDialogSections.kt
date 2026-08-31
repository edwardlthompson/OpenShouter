package org.openshouter.ui.astro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.foss.goldenpath.R
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RepeatDaysSection(
    selectedDays: Set<DayOfWeek>,
    onDaysChange: (Set<DayOfWeek>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.astro_field_repeat),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedDays.isEmpty(),
                    onClick = { onDaysChange(emptySet()) },
                    label = { Text(stringResource(R.string.astro_repeat_once), fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedDays.size == 7,
                    onClick = { onDaysChange(DayOfWeek.values().toSet()) },
                    label = { Text(stringResource(R.string.astro_repeat_daily), fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedDays == setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                    ),
                    onClick = {
                        onDaysChange(
                            setOf(
                                DayOfWeek.MONDAY,
                                DayOfWeek.TUESDAY,
                                DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY,
                                DayOfWeek.FRIDAY
                            )
                        )
                    },
                    label = { Text(stringResource(R.string.astro_repeat_weekdays), fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedDays == setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                    onClick = {
                        onDaysChange(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
                    },
                    label = { Text(stringResource(R.string.astro_repeat_weekends), fontSize = 12.sp) }
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DayOfWeek.values().forEach { d ->
                    val isSelected = selectedDays.contains(d)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onDaysChange(if (isSelected) selectedDays - d else selectedDays + d)
                        },
                        label = {
                            Text(
                                text = d.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AudioSettingsSection(
    toneEnabled: Boolean,
    onToneChange: (Boolean) -> Unit,
    toneTitle: String,
    onChooseTone: () -> Unit,
    ttsEnabled: Boolean,
    onTtsChange: (Boolean) -> Unit,
    vibrateEnabled: Boolean,
    onVibrateChange: (Boolean) -> Unit,
    mathUnlockEnabled: Boolean,
    onMathUnlockChange: (Boolean) -> Unit,
    snoozeMinutes: Int,
    onSnoozeChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.astro_alarm_actions_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.astro_toggle_tone), fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(R.string.astro_desc_play_tone),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = toneEnabled, onCheckedChange = onToneChange)
            }

            if (toneEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.astro_alarm_sound),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = toneTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = onChooseTone,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(stringResource(R.string.astro_action_choose_sound), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.astro_toggle_tts), fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(R.string.astro_desc_speak_event),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = ttsEnabled, onCheckedChange = onTtsChange)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.astro_toggle_vibrate), fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(R.string.astro_desc_vibrate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = vibrateEnabled, onCheckedChange = onVibrateChange)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.astro_toggle_math_unlock), fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(R.string.astro_desc_math_unlock),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = mathUnlockEnabled, onCheckedChange = onMathUnlockChange)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.astro_snooze_duration_title),
                    fontWeight = FontWeight.Medium
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { mins ->
                        FilterChip(
                            selected = snoozeMinutes == mins,
                            onClick = { onSnoozeChange(mins) },
                            label = { Text("${mins}m", fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    }
}
