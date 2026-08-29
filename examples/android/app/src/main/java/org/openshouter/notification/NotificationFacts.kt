package org.openshouter.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import org.openshouter.domain.NotificationRank

internal class RepeatClock {
    var lastKey: String? = null
    var lastAt: Long = 0L
    var lastRecordedKey: String? = null
    var lastRecordedAt: Long = 0L
    private val appNameAt = HashMap<String, Long>()

    fun appNameAt(pkg: String): Long = appNameAt[pkg] ?: 0L

    fun markAppName(pkg: String, now: Long) {
        if (pkg.isNotBlank()) appNameAt[pkg] = now
    }
}

internal data class NotificationFacts(
    val app: String,
    val title: String,
    val text: String,
    val tokens: Map<String, String>,
    val isGroup: Boolean,
    val people: String,
    val postedAt: Long,
    val rank: Int,
    val categoryCall: Boolean,
    val isOngoing: Boolean,
    val isTest: Boolean,
    val channelId: String = "",
    val channelName: String = "",
    val notificationKey: String = "",
    val callType: Int? = null,
) {
    companion object {
        fun from(
            sbn: StatusBarNotification,
            nm: NotificationManager?,
            channelName: String = "",
        ): NotificationFacts {
            val extras = sbn.notification.extras
            val channelId = sbn.notification.channelId.orEmpty()
            val channelImp = channelId
                .takeIf { it.isNotEmpty() }
                ?.let { nm?.getNotificationChannel(it)?.importance } ?: 3
            return NotificationFacts(
                app = sbn.packageName,
                title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
                text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
                tokens = extrasTokens(extras),
                isGroup = sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
                people = extras.getCharSequence(Notification.EXTRA_PEOPLE)?.toString().orEmpty(),
                postedAt = sbn.postTime,
                rank = NotificationRank.effective(sbn.notification.priority, channelImp),
                categoryCall = sbn.notification.category == Notification.CATEGORY_CALL,
                isOngoing = sbn.isOngoing,
                isTest = TestNotification.isSelfTest(sbn.packageName, sbn.notification.channelId),
                channelId = channelId,
                channelName = channelName,
                notificationKey = sbn.key.orEmpty(),
                callType = runCatching {
                    if (extras.containsKey(Notification.EXTRA_CALL_TYPE)) {
                        extras.getInt(Notification.EXTRA_CALL_TYPE)
                    } else {
                        null
                    }
                }.getOrNull(),
            )
        }

        fun label(pm: PackageManager, app: String): String = runCatching {
            pm.getApplicationLabel(pm.getApplicationInfo(app, 0)).toString()
        }.getOrDefault(app)

        private fun extrasTokens(extras: android.os.Bundle): Map<String, String> = mapOf(
            "ticker" to extras.getCharSequence("android.tickerText")?.toString().orEmpty(),
            "subtext" to extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty(),
            "bigtext" to extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty(),
            "info" to extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString().orEmpty(),
            "bigtitle" to extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString().orEmpty(),
            "bigsummary" to extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString().orEmpty(),
            "lines" to extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.joinToString(" ") { it.toString() }.orEmpty(),
        )
    }
}
