package org.openshouter.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import org.openshouter.domain.HistoryChannelTarget

/** Opens the posting app's notification channel in system settings (highlighted when possible). */
object NotificationChannelSettings {
    const val FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
    const val SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args"

    fun intent(packageName: String, channelId: String): Intent? {
        val pkg = HistoryChannelTarget.packageOrNull(packageName) ?: return null
        val channel = HistoryChannelTarget.highlightKey(channelId)
        return if (channel != null) channelPage(pkg, channel) else appPage(pkg, null)
    }

    fun fallback(packageName: String, channelId: String): Intent? {
        val pkg = HistoryChannelTarget.packageOrNull(packageName) ?: return null
        val channel = HistoryChannelTarget.highlightKey(channelId) ?: return null
        return appPage(pkg, channel)
    }

    fun launch(context: Context, packageName: String, channelId: String) {
        val primary = intent(packageName, channelId) ?: return
        if (runCatching { context.startActivity(primary) }.isSuccess) return
        val second = fallback(packageName, channelId) ?: return
        runCatching { context.startActivity(second) }
    }

    private fun channelPage(pkg: String, channel: String): Intent =
        highlight(
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
                .putExtra(Settings.EXTRA_CHANNEL_ID, channel),
            channel,
        )

    private fun appPage(pkg: String, channel: String?): Intent {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
        if (channel != null) {
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel)
            return highlight(intent, channel)
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun highlight(intent: Intent, channel: String): Intent {
        val args = Bundle().apply { putString(FRAGMENT_ARG_KEY, channel) }
        return intent
            .putExtra(FRAGMENT_ARG_KEY, channel)
            .putExtra(SHOW_FRAGMENT_ARGS, args)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
