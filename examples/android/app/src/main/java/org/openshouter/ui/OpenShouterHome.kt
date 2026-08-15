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
import org.openshouter.apps.InstalledAppCatalog
import org.openshouter.domain.AppSettings
import org.openshouter.service.OpenShouterEntryPoint
import org.openshouter.service.OpenShouterRuntime
import org.openshouter.ui.home.OpenShouterPanes
import org.openshouter.ui.home.Pane

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
    val history by ep.history().recent().collectAsStateWithLifecycle(emptyList())
    val regexRules by ep.regex().all().collectAsStateWithLifecycle(emptyList())
    val reminders by ep.reminders().all().collectAsStateWithLifecycle(emptyList())
    val installedApps = remember(context) { InstalledAppCatalog.list(context) }
    var pane by remember { mutableStateOf(Pane.Home) }
    var showSpoken by remember { mutableStateOf(false) }
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
    OpenShouterPanes(
        pane = pane,
        settings = settings,
        places = places,
        appRules = appRules,
        history = history,
        regexRules = regexRules,
        reminders = reminders,
        installedApps = installedApps,
        showSpoken = showSpoken,
        showSetup = pane == Pane.Setup || (pane == Pane.Home && !settings.setupComplete),
        ep = ep,
        scope = scope,
        onPane = { pane = it },
        onShowSpoken = { showSpoken = it },
        modifier = modifier,
    )
}
