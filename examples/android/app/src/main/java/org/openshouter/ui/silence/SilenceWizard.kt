package org.openshouter.ui.silence

import android.Manifest
import android.media.RingtoneManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.content.pm.PackageManager
import dev.foss.goldenpath.R
import org.openshouter.silence.OemSilenceHints
import org.openshouter.silence.SilentDefaults
import org.openshouter.silence.SilentPack
import org.openshouter.silence.SoundSettingsIntents
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.setup.SetupRow

@Composable
fun SilenceWizard(onOpenLeaks: (() -> Unit)? = null) {
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
    val storageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { tick++ }
    val installed = remember(tick) { SilentPack.installed(context) }
    val canWrite = remember(tick) { SilentDefaults.canWrite(context) }
    MenuBody { Text(stringResource(R.string.silence_help)) }
    if (OemSilenceHints.currentNeedsSilentFile()) {
        MenuBody { Text(stringResource(R.string.silence_oem_hint)) }
    }
    SetupRow(R.string.silence_install, installed) {
        if (Build.VERSION.SDK_INT < 29 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            storageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return@SetupRow
        }
        SilentPack.install(context)
        tick++
    }
    MenuLink(stringResource(R.string.silence_open_sounds), {
        runCatching { context.startActivity(SoundSettingsIntents.soundSettings()) }
    }, showDivider = true)
    MenuLink(stringResource(R.string.silence_open_ringtone), {
        runCatching { context.startActivity(SoundSettingsIntents.ringtonePicker(RingtoneManager.TYPE_RINGTONE)) }
    }, showDivider = true)
    SetupRow(R.string.silence_write_settings, canWrite) {
        runCatching { context.startActivity(SoundSettingsIntents.writeSettings(context.packageName)) }
    }
    MenuLink(stringResource(R.string.silence_set_notification), {
        val uri = SilentPack.install(context) ?: return@MenuLink
        SilentDefaults.setNotification(context, uri)
        tick++
    }, showDivider = true)
    MenuLink(stringResource(R.string.silence_set_ringtone), {
        val uri = SilentPack.install(context) ?: return@MenuLink
        SilentDefaults.setRingtone(context, uri)
        tick++
    }, showDivider = true)
    if (onOpenLeaks != null) {
        MenuLink(stringResource(R.string.silence_open_leaks), onOpenLeaks, showDivider = true)
    }
}
