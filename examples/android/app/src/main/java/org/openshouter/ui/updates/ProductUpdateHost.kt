package org.openshouter.ui.updates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import dev.foss.goldenpath.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openshouter.updates.DonateLinks
import org.openshouter.updates.GithubRelease
import org.openshouter.updates.ProductUpdate
import org.openshouter.updates.UpdatePrefs

private sealed class LaunchPrompt {
    data object Donate : LaunchPrompt()
    data class Update(val version: String, val url: String) : LaunchPrompt()
}

@Composable
fun ProductUpdateHost() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val prefs = remember { UpdatePrefs(context) }
    val current = BuildConfig.VERSION_NAME
    var prompt by remember { mutableStateOf<LaunchPrompt?>(null) }

    LaunchedEffect(Unit) {
        if (ProductUpdate.shouldNudgeDonate(prefs.lastSeenVersion(), current)) {
            prompt = LaunchPrompt.Donate
            return@LaunchedEffect
        }
        prefs.markVersionSeen(current)
        val now = System.currentTimeMillis()
        if (!ProductUpdate.shouldCheckDaily(prefs.lastCheckAt(), now)) return@LaunchedEffect
        val release = withContext(Dispatchers.IO) { GithubRelease.fetchLatest(current) }
        prefs.markChecked(now)
        val asset = release?.let { ProductUpdate.selectApkAsset(it.assets) } ?: return@LaunchedEffect
        if (!ProductUpdate.shouldPromptUpdate(current, asset.version, prefs.dismissedVersion())) {
            return@LaunchedEffect
        }
        prompt = LaunchPrompt.Update(
            asset.version,
            ProductUpdate.installUrl(asset.url, release.htmlUrl),
        )
    }

    when (val shown = prompt) {
        LaunchPrompt.Donate -> DonateNudgeDialog(
            onDonate = {
                prefs.markVersionSeen(current)
                prompt = null
                uriHandler.openUri(DonateLinks.VENMO_URL)
            },
            onLater = {
                prefs.markVersionSeen(current)
                prompt = null
            },
        )
        is LaunchPrompt.Update -> UpdateAvailableDialog(
            version = shown.version,
            onInstall = {
                prefs.markChecked(System.currentTimeMillis(), shown.version)
                prompt = null
                uriHandler.openUri(shown.url)
            },
            onLater = {
                prefs.markChecked(System.currentTimeMillis(), shown.version)
                prompt = null
            },
        )
        null -> Unit
    }
}
