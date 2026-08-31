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
import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.AstroAlarm
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AstroAlarmRow(
    alarm: AstroAlarm,
    nextFireFormatted: String?,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (alarm.target) {
                        is AlarmTarget.Solar -> "☀️"
                        is AlarmTarget.Lunar -> "🌙"
                        is AlarmTarget.CustomClock -> "⏰"
                    }
                    Text(text = icon, fontSize = 24.sp)
                    Column {
                        Text(
                            text = alarm.label,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (nextFireFormatted != null && alarm.enabled) {
                            Text(
                                text = stringResource(R.string.astro_next_fire_prefix, nextFireFormatted),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (!alarm.enabled) {
                            Text(
                                text = stringResource(R.string.astro_status_disabled),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                Switch(checked = alarm.enabled, onCheckedChange = onToggle)
            }

            Spacer(modifier = Modifier.height(10.dp))
            DaysChipRow(selectedDays = alarm.daysOfWeek)

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BadgeText(stringResource(if (alarm.toneEnabled) R.string.astro_badge_tone_on else R.string.astro_badge_tone_off))
                    BadgeText(stringResource(if (alarm.ttsEnabled) R.string.astro_badge_tts_on else R.string.astro_badge_tts_off))
                    if (alarm.vibrateEnabled) BadgeText(stringResource(R.string.astro_badge_vibrate))
                    if (alarm.mathUnlockEnabled) BadgeText(stringResource(R.string.astro_badge_math))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(stringResource(R.string.astro_action_edit), fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            stringResource(R.string.astro_action_delete),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DaysChipRow(selectedDays: Set<DayOfWeek>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (selectedDays.isEmpty()) {
            BadgeText(stringResource(R.string.astro_repeat_once))
        } else {
            DayOfWeek.values().forEach { d ->
                val isSelected = selectedDays.contains(d)
                val shortName = d.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = shortName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeText(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
