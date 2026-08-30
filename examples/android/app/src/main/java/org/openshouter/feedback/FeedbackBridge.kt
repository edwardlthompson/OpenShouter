package org.openshouter.feedback

import android.content.Context
import dev.foss.goldenpath.about.ReleaseTagFetcher
import org.openshouter.crashcapture.PendingCrashStore

class FeedbackBridge(context: Context) {
    private val app = context.applicationContext
    val prefs = FeedbackPrefs(app)
    val store = PendingCrashStore(app)

    fun initialKind(): String? = if (store.read() != null) "bug" else null

    fun pendingStack(): String? = store.read()?.stack

    fun releaseRepo(): String {
        val fromAsset = ReleaseTagFetcher.loadReleaseRepo(app)
        if (!fromAsset.isNullOrBlank()) return fromAsset
        return DEFAULT_REPO
    }

    companion object {
        const val DEFAULT_REPO = "edwardlthompson/OpenShouter"

    }
}
