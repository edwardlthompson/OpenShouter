package org.openshouter.ui.setup

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.setup.SetupChecks

@Composable
fun SetupScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
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
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { tick++ }
    val listenerOn = remember(tick) { SetupChecks.listenerEnabled(context) }
    val notifyOn = remember(tick) { SetupChecks.notifyGranted(context) }
    val phoneOn = remember(tick) { SetupChecks.granted(context, Manifest.permission.READ_PHONE_STATE) }
    val contactsOn = remember(tick) { SetupChecks.granted(context, Manifest.permission.READ_CONTACTS) }
    val logOn = remember(tick) { SetupChecks.granted(context, Manifest.permission.READ_CALL_LOG) }
    val fineOn = remember(tick) { SetupChecks.granted(context, Manifest.permission.ACCESS_FINE_LOCATION) }
    val bgOn = remember(tick) {
        Build.VERSION.SDK_INT < 29 || SetupChecks.granted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    val batteryOn = remember(tick) { SetupChecks.batteryUnrestricted(context) }
    val exactOn = remember(tick) { SetupChecks.exactAlarmsAllowed(context) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.setup_body), style = MaterialTheme.typography.bodyLarge)
        SetupRow(R.string.setup_listener, listenerOn) { SetupChecks.openListener(context) }
        if (Build.VERSION.SDK_INT >= 33) {
            SetupRow(R.string.setup_notify, notifyOn) {
                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        SetupRow(R.string.setup_phone, phoneOn) { permLauncher.launch(Manifest.permission.READ_PHONE_STATE) }
        SetupRow(R.string.setup_contacts, contactsOn) { permLauncher.launch(Manifest.permission.READ_CONTACTS) }
        SetupRow(R.string.setup_call_log, logOn) { permLauncher.launch(Manifest.permission.READ_CALL_LOG) }
        SetupRow(R.string.setup_location, fineOn) { permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
        if (Build.VERSION.SDK_INT >= 29) {
            SetupRow(R.string.setup_background, bgOn) {
                permLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
        SetupRow(R.string.setup_battery, batteryOn) { SetupChecks.requestBatteryUnrestricted(context) }
        SetupRow(R.string.setup_battery_settings, batteryOn) { SetupChecks.openAppDetails(context) }
        SetupRow(R.string.setup_exact_alarms, exactOn) { SetupChecks.requestExactAlarms(context) }
        Button(onClick = onContinue, modifier = Modifier.bottomInsetPadding().fillMaxWidth()) {
            Text(stringResource(R.string.setup_continue))
        }
    }
}

@Composable
private fun SetupRow(labelRes: Int, granted: Boolean, onClick: () -> Unit) {
    val label = stringResource(labelRes)
    val status = stringResource(if (granted) R.string.setup_granted else R.string.setup_needed)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(status, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onClick) {
            Text(stringResource(R.string.setup_activate))
        }
    }
}
