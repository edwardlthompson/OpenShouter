package org.openshouter.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import org.openshouter.apps.InstalledAppCatalog
import org.openshouter.domain.AppSettings
import org.openshouter.places.PlaceHere
import org.openshouter.service.OpenShouterEntryPoint
import org.openshouter.service.OpenShouterRuntime
import org.openshouter.ui.apps.AppSpeakScreen
import org.openshouter.ui.dashboard.DashboardScreen
import org.openshouter.ui.places.PlacesScreen
import org.openshouter.ui.settings.AnnouncerSettingsScreen
import org.openshouter.ui.setup.SetupScreen

private enum class Pane { Setup, Home, Rules, Announcer, Places }

@Composable
fun OpenShouterHome(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ep = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, OpenShouterEntryPoint::class.java)
    }
    val settingsState by produceState<AppSettings?>(initialValue = null, ep) {
        ep.settings().settings.collect { value = it }
    }
    val settings = settingsState ?: return
    val places by ep.places().all().collectAsStateWithLifecycle(emptyList())
    val appRules by ep.appSpeak().rules.collectAsStateWithLifecycle(emptyList())
    val installedApps = remember(context) { InstalledAppCatalog.list(context) }
    var pane by remember { mutableStateOf(Pane.Home) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        ep.appSpeak().importWhitelist(ep.settings().snapshot())
    }
    androidx.compose.runtime.LaunchedEffect(
        settings.announcerEnabled,
        settings.timeShoutEnabled,
        settings.timeShoutIntervalMinutes,
    ) {
        if (settings.announcerEnabled || settings.timeShoutEnabled) {
            OpenShouterRuntime.ensureStarted(context)
        }
        ep.timeShout().sync(settings)
    }
    val showSetup = pane == Pane.Setup || (pane == Pane.Home && !settings.setupComplete)
    when {
        showSetup -> SetupScreen(
            onContinue = {
                scope.launch { ep.settings().setSetupComplete(true) }
                pane = Pane.Home
            },
            modifier = modifier,
        )
        pane == Pane.Home -> DashboardScreen(
            announcerEnabled = settings.announcerEnabled,
            onAnnouncerChange = { on -> scope.launch { ep.settings().setEnabled(on) } },
            onOpenSetup = { pane = Pane.Setup },
            onOpenRules = { pane = Pane.Rules },
            onOpenAnnouncerSettings = { pane = Pane.Announcer },
            onOpenPlaces = { pane = Pane.Places },
            modifier = modifier,
        )
        pane == Pane.Rules -> AppSpeakScreen(
            settings = settings,
            rules = appRules,
            apps = installedApps,
            onFormatChange = { value -> scope.launch { ep.settings().setFormat(value) } },
            onRuleChange = { pkg, name, notif ->
                scope.launch { ep.appSpeak().set(pkg, name, notif) }
            },
            onBack = { pane = Pane.Home },
            modifier = modifier,
        )
        pane == Pane.Announcer -> AnnouncerSettingsScreen(
            settings = settings,
            onQuiet = { on ->
                scope.launch {
                    ep.settings().setQuietHours(on, settings.quietStartMinutes, settings.quietEndMinutes, settings.quietDays)
                }
            },
            onScreenOffOnly = { on ->
                scope.launch { ep.settings().setAudioGate(on, settings.headsetOnly) }
            },
            onHeadsetOnly = { on ->
                scope.launch { ep.settings().setAudioGate(settings.screenOffOnly, on) }
            },
            onShake = { on -> scope.launch { ep.settings().setGestures(on, settings.flipToMute, settings.muteOnScreenOn, settings.muteOnScreenOff) } },
            onFlip = { on -> scope.launch { ep.settings().setGestures(settings.shakeToSilence, on, settings.muteOnScreenOn, settings.muteOnScreenOff) } },
            onMuteScreenOn = { on -> scope.launch { ep.settings().setGestures(settings.shakeToSilence, settings.flipToMute, on, settings.muteOnScreenOff) } },
            onMuteScreenOff = { on -> scope.launch { ep.settings().setGestures(settings.shakeToSilence, settings.flipToMute, settings.muteOnScreenOn, on) } },
            onCalls = { on -> scope.launch { ep.settings().setCalls(on) } },
            onNotifications = { on -> scope.launch { ep.settings().setNotifications(on) } },
            onTimeShout = { on ->
                scope.launch { ep.settings().setTimeShout(on, settings.timeShoutIntervalMinutes) }
            },
            onBack = { pane = Pane.Home },
            modifier = modifier,
        )
        pane == Pane.Places -> PlacesScreen(
            places = places,
            onSavePlace = { label ->
                scope.launch { PlaceHere.save(context, ep, label) }
            },
            onDelete = { id -> scope.launch { ep.places().delete(id) } },
            onBack = { pane = Pane.Home },
            modifier = modifier,
        )
    }
}
