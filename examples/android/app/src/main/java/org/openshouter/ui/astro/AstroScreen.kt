package org.openshouter.ui.astro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.foss.goldenpath.R
import org.openshouter.astro.alarm.AstroAlarmScheduler
import org.openshouter.astro.alarm.AstroAlarmStore
import org.openshouter.astro.alarm.AstroNextFire
import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.AstroAlarm
import org.openshouter.astro.model.LunarEventType
import org.openshouter.astro.model.SolarEventType
import org.openshouter.astro.place.AstroPlace
import org.openshouter.astro.place.AstroPlaceFinder
import org.openshouter.astro.place.AstroPlaceStore
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
fun AstroScreen(
    placeStore: AstroPlaceStore,
    alarmStore: AstroAlarmStore,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val place by placeStore.place.collectAsState()
    val alarms by alarmStore.alarms.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var citySuggestions by remember { mutableStateOf<List<AstroPlace>>(emptyList()) }
    var editingAlarm by remember { mutableStateOf<AstroAlarm?>(null) }
    var showAddDialogWithTarget by remember { mutableStateOf<AlarmTarget?>(null) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            citySuggestions = AstroPlaceFinder.searchCities(context, searchQuery)
        } else {
            citySuggestions = emptyList()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.astro_screen_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LocationCard(
                    place = place,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    suggestions = citySuggestions,
                    onSelectCity = { selected ->
                        placeStore.set(selected)
                        searchQuery = ""
                        citySuggestions = emptyList()
                        AstroAlarmScheduler.rescheduleAll(context)
                    },
                    onUseGps = {
                        val loc = AstroPlaceFinder.resolveFromLocation(context)
                        if (loc != null) {
                            placeStore.set(loc)
                            AstroAlarmScheduler.rescheduleAll(context)
                        }
                    }
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.astro_section_clock),
                    onAdd = { showAddDialogWithTarget = AlarmTarget.CustomClock(7, 0) }
                )
            }
            val clockAlarms = alarms.filter { it.target is AlarmTarget.CustomClock }
            if (clockAlarms.isEmpty()) {
                item { EmptySectionNote(stringResource(R.string.astro_empty_clock)) }
            } else {
                items(clockAlarms, key = { it.id }) { alarm ->
                    val nextInstant = AstroNextFire.nextInstant(alarm, place)
                    val formatted = nextInstant?.let { formatInstant(it, place) }
                    AstroAlarmRow(
                        alarm = alarm,
                        nextFireFormatted = formatted,
                        onToggle = { enabled ->
                            alarmStore.toggle(alarm.id, enabled)
                            AstroAlarmScheduler.rescheduleAll(context)
                        },
                        onEdit = { editingAlarm = alarm },
                        onDelete = {
                            alarmStore.delete(alarm.id)
                            AstroAlarmScheduler.rescheduleAll(context)
                        }
                    )
                }
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.astro_section_solar),
                    onAdd = { showAddDialogWithTarget = AlarmTarget.Solar(SolarEventType.Sunrise, 0) }
                )
            }
            val solarAlarms = alarms.filter { it.target is AlarmTarget.Solar }
            if (solarAlarms.isEmpty()) {
                item { EmptySectionNote(stringResource(R.string.astro_empty_solar)) }
            } else {
                items(solarAlarms, key = { it.id }) { alarm ->
                    val nextInstant = AstroNextFire.nextInstant(alarm, place)
                    val formatted = nextInstant?.let { formatInstant(it, place) }
                    AstroAlarmRow(
                        alarm = alarm,
                        nextFireFormatted = formatted,
                        onToggle = { enabled ->
                            alarmStore.toggle(alarm.id, enabled)
                            AstroAlarmScheduler.rescheduleAll(context)
                        },
                        onEdit = { editingAlarm = alarm },
                        onDelete = {
                            alarmStore.delete(alarm.id)
                            AstroAlarmScheduler.rescheduleAll(context)
                        }
                    )
                }
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.astro_section_lunar),
                    onAdd = { showAddDialogWithTarget = AlarmTarget.Lunar(LunarEventType.FullMoon, 0) }
                )
            }
            val lunarAlarms = alarms.filter { it.target is AlarmTarget.Lunar }
            if (lunarAlarms.isEmpty()) {
                item { EmptySectionNote(stringResource(R.string.astro_empty_lunar)) }
            } else {
                items(lunarAlarms, key = { it.id }) { alarm ->
                    val nextInstant = AstroNextFire.nextInstant(alarm, place)
                    val formatted = nextInstant?.let { formatInstant(it, place) }
                    AstroAlarmRow(
                        alarm = alarm,
                        nextFireFormatted = formatted,
                        onToggle = { enabled ->
                            alarmStore.toggle(alarm.id, enabled)
                            AstroAlarmScheduler.rescheduleAll(context)
                        },
                        onEdit = { editingAlarm = alarm },
                        onDelete = {
                            alarmStore.delete(alarm.id)
                            AstroAlarmScheduler.rescheduleAll(context)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showAddDialogWithTarget != null) {
        AstroEditDialog(
            initialAlarm = null,
            defaultTarget = showAddDialogWithTarget!!,
            onDismiss = { showAddDialogWithTarget = null },
            onSave = { newAlarm ->
                alarmStore.save(newAlarm)
                AstroAlarmScheduler.rescheduleAll(context)
                showAddDialogWithTarget = null
            }
        )
    }

    if (editingAlarm != null) {
        AstroEditDialog(
            initialAlarm = editingAlarm,
            defaultTarget = editingAlarm!!.target,
            onDismiss = { editingAlarm = null },
            onSave = { updated ->
                alarmStore.save(updated)
                AstroAlarmScheduler.rescheduleAll(context)
                editingAlarm = null
            }
        )
    }
}

@Composable
private fun LocationCard(
    place: AstroPlace?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    suggestions: List<AstroPlace>,
    onSelectCity: (AstroPlace) -> Unit,
    onUseGps: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.astro_place_card_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = place?.cityName ?: stringResource(R.string.astro_no_place_set),
                color = if (place != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text(stringResource(R.string.astro_search_city_hint)) },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onUseGps, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Text(stringResource(R.string.astro_btn_locate))
                }
            }
            suggestions.forEach { sugg ->
                TextButton(onClick = { onSelectCity(sugg) }, modifier = Modifier.fillMaxWidth()) {
                    Text(sugg.cityName, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Button(onClick = onAdd) {
            Text(stringResource(R.string.astro_action_add))
        }
    }
}

@Composable
private fun EmptySectionNote(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

private fun formatInstant(instant: Instant, place: AstroPlace?): String {
    val zone = place?.zone ?: java.time.ZoneId.systemDefault()
    val fmt = DateTimeFormatter.ofPattern("EEE HH:mm").withZone(zone)
    return fmt.format(instant)
}
