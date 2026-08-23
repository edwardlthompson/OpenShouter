package org.openshouter.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.foss.goldenpath.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.openshouter.apps.InstalledApp
import org.openshouter.data.HistoryEntity
import org.openshouter.data.PlaceEntity
import org.openshouter.data.RegexEntity
import org.openshouter.data.ReminderEntity
import org.openshouter.domain.AppSettings
import org.openshouter.domain.AppSpeakRule
import org.openshouter.domain.SpokenEvent
import org.openshouter.backup.BackupImport
import org.openshouter.backup.BackupScreen
import org.openshouter.backup.SettingsBackup
import org.openshouter.bluetooth.BluetoothShoutScreen
import org.openshouter.calendar.CalendarShoutScreen
import org.openshouter.contacts.ContactRulesScreen
import org.openshouter.message.MessageChannelScreen
import org.openshouter.power.PowerSettings
import org.openshouter.reminder.ReminderAlarms
import org.openshouter.places.PlaceHere
import org.openshouter.ui.overrides.OverrideScreen
import org.openshouter.service.OpenShouterEntryPoint
import org.openshouter.ui.apps.AppSpeakScreen
import org.openshouter.ui.dashboard.DashboardScreen
import org.openshouter.ui.filters.FiltersScreen
import org.openshouter.ui.history.HistoryScreen
import org.openshouter.ui.places.PlacesScreen
import org.openshouter.ui.quiet.QuietHoursScreen
import org.openshouter.ui.setup.SetupScreen
import org.openshouter.reminder.ReminderScreen
import org.openshouter.reminder.reminderDefaults
import org.openshouter.time.TimeShoutScreen
import org.openshouter.notification.TestNotification
import org.openshouter.oem.OemScreen
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.tts.TtsSettingsScreen

enum class Pane { Setup, Home, Rules, Announcer, Quiet, History, Filters, Tts, Time, Reminders, Backup, Overrides, Places, Oem, Contacts, Messages, Power, Calendar, Bluetooth }

@Composable
fun OpenShouterPanes(
    pane: Pane,
    settings: AppSettings,
    places: List<PlaceEntity>,
    appRules: List<AppSpeakRule>,
    history: List<HistoryEntity>,
    regexRules: List<RegexEntity>,
    reminders: List<ReminderEntity>,
    installedApps: List<InstalledApp>,
    showSpoken: Boolean,
    showSetup: Boolean,
    ep: OpenShouterEntryPoint,
    scope: CoroutineScope,
    onPane: (Pane) -> Unit,
    onShowSpoken: (Boolean) -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    when {
        showSetup -> SetupScreen(
            onContinue = {
                scope.launch { ep.settings().setSetupComplete(true) }
                onPane(Pane.Home)
            },
            appCount = appRules.count { it.active },
            onPickApps = { onPane(Pane.Rules) },
            onImportInstalled = { BackupImport.applyInstalled(ep, context) },
            onImportBytes = { bytes -> BackupImport.applyBytes(ep, settings, bytes, context) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Home -> DashboardScreen(
            announcerEnabled = settings.announcerEnabled,
            onAnnouncerChange = { on -> scope.launch { ep.settings().setEnabled(on) } },
            onOpenSetup = { onPane(Pane.Setup) },
            onOpenRules = { onPane(Pane.Rules) },
            onOpenAnnouncerSettings = { onPane(Pane.Announcer) },
            onOpenHistory = { onPane(Pane.History) },
            onOpenFilters = { onPane(Pane.Filters) },
            onOpenTts = { onPane(Pane.Tts) },
            onOpenReminders = { onPane(Pane.Reminders) },
            onOpenBackup = { onPane(Pane.Backup) },
            onOpenOverrides = { onPane(Pane.Overrides) },
            onOpenPlaces = { onPane(Pane.Places) },
            onOpenOem = { onPane(Pane.Oem) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Rules -> AppSpeakScreen(
            rules = appRules,
            apps = installedApps,
            onRuleChange = { pkg, name, notif ->
                scope.launch { ep.appSpeak().set(pkg, name, notif) }
            },
            onBulkChange = { pkgs, name, notif ->
                scope.launch { ep.appSpeak().setMany(pkgs, name, notif) }
            },
            onBack = { onPane(if (settings.setupComplete) Pane.Home else Pane.Setup) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Announcer -> AnnouncerPane(settings, ep, scope, onPane, scrollStore, modifier)
        pane == Pane.Time -> TimeShoutScreen(
            settings = settings,
            onChange = { enabled, interval, exact ->
                scope.launch { ep.settings().setTimeShout(enabled, interval, exact) }
            },
            onFormat = { value -> scope.launch { ep.sprint13().setTimeFormat(value) } },
            onHourStyle = { style -> scope.launch { ep.sprint13().setTimeHourStyle(style) } },
            onBack = { onPane(Pane.Announcer) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Quiet -> QuietHoursScreen(
            settings = settings,
            onChange = { enabled, start, end, days ->
                scope.launch { ep.settings().setQuietHours(enabled, start, end, days) }
            },
            onBack = { onPane(Pane.Announcer) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.History -> HistoryScreen(
            rows = history,
            showSpoken = showSpoken,
            onShowSpoken = onShowSpoken,
            onClear = { scope.launch { ep.history().clear() } },
            onBack = { onPane(Pane.Home) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Filters -> FiltersScreen(
            rules = regexRules,
            policy = settings.notificationPolicy,
            onPolicy = { policy -> scope.launch { ep.settings().setNotificationPolicy(policy) } },
            onAdd = { pattern, action, replacement ->
                scope.launch {
                    ep.regex().insert(RegexEntity(pattern = pattern, action = action, replacement = replacement))
                }
            },
            onDelete = { id -> scope.launch { ep.regex().delete(id) } },
            onBack = { onPane(Pane.Home) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Tts -> TtsSettingsScreen(
            settings = settings,
            onPlayback = { policy -> scope.launch { ep.settings().setTtsPlayback(policy) } },
            onFormatChange = { value -> scope.launch { ep.settings().setFormat(value) } },
            onDeviceState = { policy -> scope.launch { ep.settings().setDeviceState(policy) } },
            onTest = {
                ep.tts().speak(
                    SpokenEvent(
                        SpokenEvent.Kind.NOTIFICATION,
                        context.getString(R.string.tts_test_phrase),
                        stream = settings.ttsPlayback.stream,
                    ),
                    immediate = true,
                )
            },
            onPostTest = { TestNotification.post(context) },
            onOpenSystemTts = {
                runCatching { context.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            },
            languages = ep.tts().languageTags(settings.ttsPlayback.voice.engine),
            voices = ep.tts().voices(settings.ttsPlayback.voice.engine),
            engineGen = ep.tts().engineGen,
            loadLanguages = { ep.tts().languageTags(settings.ttsPlayback.voice.engine) },
            loadVoices = { ep.tts().voices(settings.ttsPlayback.voice.engine) },
            engines = ep.tts().installedEngines(),
            downloads = ep.tts().downloadOffers(),
            onOpenUrl = { url ->
                if (!url.startsWith("https://")) return@TtsSettingsScreen
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            },
            onOpenApp = { pkg ->
                val launch = context.packageManager.getLaunchIntentForPackage(pkg) ?: return@TtsSettingsScreen
                runCatching { context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            },
            onChannelStates = { map -> scope.launch { ep.sprint13().setChannelStates(map) } },
            onBack = { onPane(Pane.Home) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Reminders -> ReminderScreen(
            reminders = reminders,
            onAdd = { text, also, interval ->
                val row = reminderDefaults(text, System.currentTimeMillis(), also, interval) ?: return@ReminderScreen
                scope.launch {
                    val id = ep.reminders().insert(row)
                    ReminderAlarms.sync(context, ep.alarms(), listOf(row.copy(id = id)), settings.timeShoutExact)
                }
            },
            onDelete = { id ->
                scope.launch {
                    ReminderAlarms.cancel(context, ep.alarms(), id)
                    ep.reminders().delete(id)
                }
            },
            onBack = { onPane(Pane.Home) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Backup -> BackupScreen(
            onExportBytes = { SettingsBackup.toZip(settings, appRules) },
            onImportBytes = { bytes -> BackupImport.applyBytes(ep, settings, bytes, context) },
            onImportInstalled = { BackupImport.applyInstalled(ep, context) },
            onBack = { onPane(Pane.Home) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Overrides -> OverrideScreen(
            overrides = settings.appOverrides,
            onSave = { row -> scope.launch { ep.sprint13().setOverride(row) } },
            onBack = { onPane(Pane.Home) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Contacts -> ContactRulesScreen(
            rule = settings.contactRule,
            speakUnknownCall = settings.missedCall.speakUnknown,
            speakUnknownMessage = settings.messageChannel.speakUnknown,
            onRuleChange = { rule -> scope.launch { ep.sprint13().setContactRule(rule) } },
            onSpeakUnknownCall = { on -> scope.launch { ep.settings().setMissedCall(settings.missedCall.copy(speakUnknown = on)) } },
            onSpeakUnknownMessage = { on -> scope.launch { ep.settings().setMessageChannel(settings.messageChannel.copy(speakUnknown = on)) } },
            callFormat = settings.callFormat,
            onCallFormat = { value -> scope.launch { ep.sprint13().setCallFormat(value) } },
            onBack = { onPane(Pane.Announcer) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Messages -> MessageChannelScreen(
            policy = settings.messageChannel,
            format = settings.messageFormat,
            onPolicy = { policy -> scope.launch { ep.settings().setMessageChannel(policy) } },
            onFormat = { value -> scope.launch { ep.sprint13().setMessageFormat(value) } },
            onBack = { onPane(Pane.Announcer) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Power -> PowerSettings(
            phrases = settings.batteryPhrases,
            onChange = { phrases -> scope.launch { ep.sprint13().setBatteryPhrases(phrases) } },
            onBack = { onPane(Pane.Announcer) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Calendar -> CalendarShoutScreen(
            enabled = settings.calendarShoutEnabled,
            lookaheadMinutes = settings.calendarLookaheadMinutes,
            onEnabled = { on -> scope.launch { ep.sprint15().setCalendar(on) } },
            onLookahead = { minutes -> scope.launch { ep.sprint15().setCalendarLookahead(minutes) } },
            onBack = { onPane(Pane.Announcer) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Bluetooth -> BluetoothShoutScreen(
            connectAlert = settings.bluetoothConnectAlert,
            batteryAlert = settings.bluetoothBatteryAlert,
            onConnect = { on ->
                scope.launch { ep.sprint15().setBluetooth(on, settings.bluetoothBatteryAlert) }
            },
            onBattery = { on ->
                scope.launch { ep.sprint15().setBluetooth(settings.bluetoothConnectAlert, on) }
            },
            onBack = { onPane(Pane.Announcer) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Oem -> OemScreen(
            onOpen = { intent ->
                runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            },
            onBack = { onPane(Pane.Home) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
        pane == Pane.Places -> PlacesScreen(
            places = places,
            onSavePlace = { label -> scope.launch { PlaceHere.save(context, ep, label) } },
            onDelete = { id -> scope.launch { ep.places().delete(id) } },
            onBack = { onPane(Pane.Home) },
            scrollStore = scrollStore,
            modifier = modifier,
        )
    }
}
