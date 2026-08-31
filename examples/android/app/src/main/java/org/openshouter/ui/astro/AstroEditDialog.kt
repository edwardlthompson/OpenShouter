package org.openshouter.ui.astro

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.foss.goldenpath.R
import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.AstroAlarm
import java.util.Locale
import java.util.UUID

@Composable
fun AstroEditDialog(
    initialAlarm: AstroAlarm?,
    defaultTarget: AlarmTarget,
    onDismiss: () -> Unit,
    onSave: (AstroAlarm) -> Unit
) {
    val context = LocalContext.current
    var target by remember { mutableStateOf(initialAlarm?.target ?: defaultTarget) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "") }
    var selectedDays by remember { mutableStateOf(initialAlarm?.daysOfWeek ?: emptySet()) }
    var toneEnabled by remember { mutableStateOf(initialAlarm?.toneEnabled ?: true) }
    var toneUri by remember { mutableStateOf(initialAlarm?.toneUri) }
    var ttsEnabled by remember { mutableStateOf(initialAlarm?.ttsEnabled ?: true) }
    var vibrateEnabled by remember { mutableStateOf(initialAlarm?.vibrateEnabled ?: true) }
    var mathUnlockEnabled by remember { mutableStateOf(initialAlarm?.mathUnlockEnabled ?: false) }
    var snoozeMinutes by remember { mutableStateOf(initialAlarm?.snoozeMinutes ?: 10) }

    val toneTitle = remember(toneUri) {
        if (toneUri == null) {
            context.getString(R.string.astro_sound_default)
        } else {
            runCatching {
                RingtoneManager.getRingtone(context, Uri.parse(toneUri))?.getTitle(context)
            }.getOrNull() ?: context.getString(R.string.astro_sound_default)
        }
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            toneUri = uri?.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            if (initialAlarm != null) R.string.astro_dialog_edit_title else R.string.astro_dialog_add_title
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.astro_action_cancel))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TargetTypeSelector(
                        currentTarget = target,
                        onTargetChange = { newTarget ->
                            target = newTarget
                        }
                    )

                    when (val currentTarget = target) {
                        is AlarmTarget.Solar -> {
                            SolarEventPicker(
                                selectedEvent = currentTarget.event,
                                onSelectEvent = { newEvent ->
                                    target = currentTarget.copy(event = newEvent)
                                }
                            )
                            OffsetSelector(
                                offsetMinutes = currentTarget.offsetMinutes,
                                eventName = AstroEventLabels.solarLabel(currentTarget.event),
                                onOffsetChange = { newOffset ->
                                    target = currentTarget.copy(offsetMinutes = newOffset)
                                }
                            )
                        }
                        is AlarmTarget.Lunar -> {
                            LunarEventPicker(
                                selectedEvent = currentTarget.event,
                                onSelectEvent = { newEvent ->
                                    target = currentTarget.copy(event = newEvent)
                                }
                            )
                            OffsetSelector(
                                offsetMinutes = currentTarget.offsetMinutes,
                                eventName = AstroEventLabels.lunarLabel(currentTarget.event),
                                onOffsetChange = { newOffset ->
                                    target = currentTarget.copy(offsetMinutes = newOffset)
                                }
                            )
                        }
                        is AlarmTarget.CustomClock -> {
                            ClockTimePicker(
                                hour = currentTarget.hour,
                                minute = currentTarget.minute,
                                onTimeChange = { newH, newM ->
                                    target = AlarmTarget.CustomClock(newH, newM)
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text(stringResource(R.string.astro_field_label)) },
                        placeholder = {
                            Text(
                                when (val t = target) {
                                    is AlarmTarget.CustomClock -> String.format(Locale.getDefault(), "%02d:%02d", t.hour, t.minute)
                                    is AlarmTarget.Solar -> AstroEventLabels.offsetSummary(t.offsetMinutes, AstroEventLabels.solarLabel(t.event))
                                    is AlarmTarget.Lunar -> AstroEventLabels.offsetSummary(t.offsetMinutes, AstroEventLabels.lunarLabel(t.event))
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    RepeatDaysSection(
                        selectedDays = selectedDays,
                        onDaysChange = { selectedDays = it }
                    )

                    AudioSettingsSection(
                        toneEnabled = toneEnabled,
                        onToneChange = { toneEnabled = it },
                        toneTitle = toneTitle,
                        onChooseTone = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                                    RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE or RingtoneManager.TYPE_NOTIFICATION
                                )
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.astro_alarm_sound))
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    toneUri?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                                )
                            }
                            ringtonePickerLauncher.launch(intent)
                        },
                        ttsEnabled = ttsEnabled,
                        onTtsChange = { ttsEnabled = it },
                        vibrateEnabled = vibrateEnabled,
                        onVibrateChange = { vibrateEnabled = it },
                        mathUnlockEnabled = mathUnlockEnabled,
                        onMathUnlockChange = { mathUnlockEnabled = it },
                        snoozeMinutes = snoozeMinutes,
                        onSnoozeChange = { snoozeMinutes = it }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.astro_action_cancel))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val finalLabel = label.ifBlank {
                                when (val t = target) {
                                    is AlarmTarget.CustomClock -> String.format(Locale.getDefault(), "%02d:%02d", t.hour, t.minute)
                                    is AlarmTarget.Solar -> AstroEventLabels.offsetSummary(t.offsetMinutes, AstroEventLabels.solarLabel(t.event))
                                    is AlarmTarget.Lunar -> AstroEventLabels.offsetSummary(t.offsetMinutes, AstroEventLabels.lunarLabel(t.event))
                                }
                            }
                            val result = AstroAlarm(
                                id = initialAlarm?.id ?: UUID.randomUUID().toString(),
                                label = finalLabel,
                                enabled = initialAlarm?.enabled ?: true,
                                target = target,
                                daysOfWeek = selectedDays,
                                toneEnabled = toneEnabled,
                                toneUri = toneUri,
                                ttsEnabled = ttsEnabled,
                                vibrateEnabled = vibrateEnabled,
                                snoozeMinutes = snoozeMinutes,
                                mathUnlockEnabled = mathUnlockEnabled
                            )
                            onSave(result)
                        }
                    ) {
                        Text(stringResource(R.string.astro_action_save))
                    }
                }
            }
        }
    }
}
