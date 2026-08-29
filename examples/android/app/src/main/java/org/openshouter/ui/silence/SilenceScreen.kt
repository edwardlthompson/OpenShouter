package org.openshouter.ui.silence

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.foss.goldenpath.R
import org.openshouter.notification.SoundLeakRescan
import org.openshouter.data.SoundLeakEntity
import org.openshouter.notification.NotificationFacts
import org.openshouter.silence.AudioSessionHint
import org.openshouter.silence.SoundEvidence
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection

@Composable
fun SilenceScreen(
    leaks: List<SoundLeakEntity>,
    onOpenChannel: (String, String) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) SoundLeakRescan.request()
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    MenuScaffold(stringResource(R.string.nav_silence), scrollStore, "silence", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            SilenceWizard()
        }
        MenuSection(stringResource(R.string.menu_section_list)) {
            MenuBody { Text(stringResource(R.string.silence_leaks_help)) }
            if (leaks.isEmpty()) {
                MenuBody { Text(stringResource(R.string.silence_leaks_empty)) }
            } else {
                leaks.forEachIndexed { index, row ->
                    val label = NotificationFacts.label(context.packageManager, row.packageName)
                    val evidence = evidenceLabel(row.evidence)
                    val detail = row.channelName.ifBlank { evidence }
                    MenuLink(
                        stringResource(R.string.silence_leak_row, label, detail),
                        {
                            val channel = row.channelId.takeUnless { it == AudioSessionHint.CHANNEL_OWN }.orEmpty()
                            onOpenChannel(row.packageName, channel)
                        },
                        supporting = evidence,
                        showDivider = index > 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun evidenceLabel(evidence: String): String {
    val res = when (evidence) {
        SoundEvidence.CHANNEL_SOUND.name -> R.string.silence_evidence_channel
        SoundEvidence.NOTIFICATION_SOUND.name -> R.string.silence_evidence_notification
        SoundEvidence.DEFAULT_SOUND.name -> R.string.silence_evidence_default
        SoundEvidence.OWN_AUDIO.name -> R.string.silence_evidence_own
        else -> R.string.silence_evidence_channel
    }
    return stringResource(res)
}
