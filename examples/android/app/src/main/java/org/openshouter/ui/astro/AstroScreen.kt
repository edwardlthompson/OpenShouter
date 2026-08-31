package org.openshouter.ui.astro

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.foss.goldenpath.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

@Composable
fun AstroScreen(
    placeStore: AstroPlaceStore,
    alarmStore: AstroAlarmStore,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val place by placeStore.place.collectAsState()
    val alarms by alarmStore.alarms.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var citySuggestions by remember { mutableStateOf<List<AstroPlace>>(emptyList()) }
    var editingAlarm by remember { mutableStateOf<AstroAlarm?>(null) }
    var showAddDialogWithTarget by remember { mutableStateOf<AlarmTarget?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    fun triggerLocate() {
        scope.launch {
            isLocating = true
            val loc = withContext(Dispatchers.IO) {
                AstroPlaceFinder.resolveLocation(context)
            }
            isLocating = false
            if (loc != null) {
                placeStore.set(loc)
                AstroAlarmScheduler.rescheduleAll(context)
                Toast.makeText(context, "Location updated: ${loc.cityName}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not acquire GPS fix. Please check location settings.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            triggerLocate()
        } else {
            Toast.makeText(context, "Location permission is required to detect sunrise/sunset for your area.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            citySuggestions = withContext(Dispatchers.IO) {
                AstroPlaceFinder.searchCities(context, searchQuery)
            }
        } else {
            citySuggestions = emptyList()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialogWithTarget = AlarmTarget.Solar(SolarEventType.Sunrise, 0) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.astro_screen_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                LocationCard(
                    place = place,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    suggestions = citySuggestions,
                    isLocating = isLocating,
                    onSelectCity = { selected ->
                        placeStore.set(selected)
                        searchQuery = ""
                        citySuggestions = emptyList()
                        AstroAlarmScheduler.rescheduleAll(context)
                    },
                    onUseGps = {
                        if (AstroPlaceFinder.hasLocationPermission(context)) {
                            triggerLocate()
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )
            }

            item {
                SectionHeader(
                    title = "☀️ " + stringResource(R.string.astro_section_solar),
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
                    title = "🌙 " + stringResource(R.string.astro_section_lunar),
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

            item {
                SectionHeader(
                    title = "⏰ " + stringResource(R.string.astro_section_clock),
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

            item { Spacer(modifier = Modifier.height(72.dp)) }
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
