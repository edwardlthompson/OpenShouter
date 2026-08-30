package org.openshouter.ui.astro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.foss.goldenpath.R
import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.AstroAlarm
import org.openshouter.astro.model.LunarEventType
import org.openshouter.astro.model.SolarEventType
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

@Composable
fun AstroEditDialog(
    initialAlarm: AstroAlarm?,
    defaultTarget: AlarmTarget,
    onDismiss: () -> Unit,
    onSave: (AstroAlarm) -> Unit
) {
    var label by remember { mutableStateOf(initialAlarm?.label ?: "") }
    var target by remember { mutableStateOf(initialAlarm?.target ?: defaultTarget) }
    var selectedDays by remember { mutableStateOf(initialAlarm?.daysOfWeek ?: emptySet()) }
    var toneEnabled by remember { mutableStateOf(initialAlarm?.toneEnabled ?: true) }
    var ttsEnabled by remember { mutableStateOf(initialAlarm?.ttsEnabled ?: true) }
    var vibrateEnabled by remember { mutableStateOf(initialAlarm?.vibrateEnabled ?: true) }
    var snoozeMinutes by remember { mutableStateOf(initialAlarm?.snoozeMinutes ?: 10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (initialAlarm != null) R.string.astro_dialog_edit_title else R.string.astro_dialog_add_title))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.astro_field_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                TargetEditor(target = target, onTargetChange = { target = it })

                Text(stringResource(R.string.astro_field_repeat), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DayOfWeek.values().forEach { d ->
                        val isSelected = selectedDays.contains(d)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (isSelected) selectedDays - d else selectedDays + d
                            },
                            label = { Text(d.getDisplayName(TextStyle.NARROW, Locale.getDefault())) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.astro_toggle_tone))
                    Switch(checked = toneEnabled, onCheckedChange = { toneEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.astro_toggle_tts))
                    Switch(checked = ttsEnabled, onCheckedChange = { ttsEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.astro_toggle_vibrate))
                    Switch(checked = vibrateEnabled, onCheckedChange = { vibrateEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalLabel = label.ifBlank {
                        when (val t = target) {
                            is AlarmTarget.CustomClock -> String.format(Locale.getDefault(), "%02d:%02d", t.hour, t.minute)
                            is AlarmTarget.Solar -> t.event.name + (if (t.offsetMinutes != 0) " (${t.offsetMinutes}m)" else "")
                            is AlarmTarget.Lunar -> t.event.name + (if (t.offsetMinutes != 0) " (${t.offsetMinutes}m)" else "")
                        }
                    }
                    val result = AstroAlarm(
                        id = initialAlarm?.id ?: UUID.randomUUID().toString(),
                        label = finalLabel,
                        enabled = initialAlarm?.enabled ?: true,
                        target = target,
                        daysOfWeek = selectedDays,
                        toneEnabled = toneEnabled,
                        toneUri = initialAlarm?.toneUri,
                        ttsEnabled = ttsEnabled,
                        vibrateEnabled = vibrateEnabled,
                        snoozeMinutes = snoozeMinutes
                    )
                    onSave(result)
                }
            ) {
                Text(stringResource(R.string.astro_action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.astro_action_cancel))
            }
        }
    )
}

@Composable
private fun TargetEditor(target: AlarmTarget, onTargetChange: (AlarmTarget) -> Unit) {
    when (target) {
        is AlarmTarget.CustomClock -> {
            var hour by remember { mutableStateOf(target.hour) }
            var minute by remember { mutableStateOf(target.minute) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.astro_time_label, hour, minute), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = { hour = (hour + 1) % 24; onTargetChange(AlarmTarget.CustomClock(hour, minute)) }) {
                    Text(stringResource(R.string.astro_btn_plus_1h))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { minute = (minute + 5) % 60; onTargetChange(AlarmTarget.CustomClock(hour, minute)) }) {
                    Text(stringResource(R.string.astro_btn_plus_5m))
                }
            }
        }
        is AlarmTarget.Solar -> {
            var offset by remember { mutableStateOf(target.offsetMinutes) }
            Text(stringResource(R.string.astro_event_name, target.event.name))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.astro_offset_label, offset))
                Row {
                    Button(onClick = { offset -= 15; onTargetChange(target.copy(offsetMinutes = offset)) }) {
                        Text(stringResource(R.string.astro_btn_minus_15m))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { offset += 15; onTargetChange(target.copy(offsetMinutes = offset)) }) {
                        Text(stringResource(R.string.astro_btn_plus_15m))
                    }
                }
            }
        }
        is AlarmTarget.Lunar -> {
            var offset by remember { mutableStateOf(target.offsetMinutes) }
            Text(stringResource(R.string.astro_event_name, target.event.name))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.astro_offset_label, offset))
                Row {
                    Button(onClick = { offset -= 15; onTargetChange(target.copy(offsetMinutes = offset)) }) {
                        Text(stringResource(R.string.astro_btn_minus_15m))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { offset += 15; onTargetChange(target.copy(offsetMinutes = offset)) }) {
                        Text(stringResource(R.string.astro_btn_plus_15m))
                    }
                }
            }
        }
    }
}
