package org.openshouter.calendar

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.foss.goldenpath.R
import org.openshouter.setup.SetupChecks
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuDropdown
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@Composable
fun CalendarShoutScreen(
    enabled: Boolean,
    lookaheadMinutes: Int,
    onEnabled: (Boolean) -> Unit,
    onLookahead: (Int) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val choices = CalendarShout.MINUTE_CHOICES.map { minutes ->
        minutes.toString() to stringResource(R.string.calendar_lookahead_min, minutes)
    }
    MenuScaffold(stringResource(R.string.nav_calendar), scrollStore, "calendar", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            CalendarEnableToggle(enabled, onEnabled, showGrantLink = true)
            MenuBody {
                Text(stringResource(R.string.calendar_help), style = MaterialTheme.typography.bodyLarge)
                MenuDropdown(
                    label = stringResource(R.string.calendar_lookahead),
                    text = choices.firstOrNull { it.first == lookaheadMinutes.toString() }?.second
                        ?: choices.first().second,
                    options = choices,
                    onSelect = { raw ->
                        onLookahead(raw.toIntOrNull() ?: CalendarShout.MINUTE_CHOICES.first())
                    },
                )
            }
        }
    }
}

@Composable
fun CalendarEnableToggle(
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
    showDivider: Boolean = false,
    showGrantLink: Boolean = false,
) {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) tick++
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    val granted = remember(tick) { SetupChecks.granted(context, Manifest.permission.READ_CALENDAR) }
    var asked by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        tick++
        asked = true
        if (ok) onEnabled(true)
    }
    val request: () -> Unit = {
        if (asked && !SetupChecks.granted(context, Manifest.permission.READ_CALENDAR)) {
            SetupChecks.openAppDetails(context)
        } else {
            launcher.launch(Manifest.permission.READ_CALENDAR)
        }
    }
    LaunchedEffect(enabled) {
        if (enabled && !SetupChecks.granted(context, Manifest.permission.READ_CALENDAR)) {
            launcher.launch(Manifest.permission.READ_CALENDAR)
        }
    }
    MenuToggle(
        stringResource(R.string.calendar_enable),
        enabled,
        onChange = { on ->
            when {
                !on -> onEnabled(false)
                granted -> onEnabled(true)
                else -> request()
            }
        },
        showDivider = showDivider,
    )
    if (showGrantLink && !granted) {
        MenuLink(stringResource(R.string.setup_calendar), request, showDivider = true)
    }
}
