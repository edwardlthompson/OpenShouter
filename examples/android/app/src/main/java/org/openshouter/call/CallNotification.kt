package org.openshouter.call

import org.openshouter.domain.AppSettings
import org.openshouter.domain.SpokenEvent
import org.openshouter.message.MessageChannel

/** VoIP / CATEGORY_CALL posts. Cellular stays on [CallMonitor]. */
object CallNotification {
    fun ignorePosted(packageName: String, ongoing: Boolean, categoryCall: Boolean): Boolean =
        ongoing && !isIncomingCall(packageName, categoryCall, ongoing)

    fun routeAsCall(
        packageName: String,
        categoryCall: Boolean,
        ongoing: Boolean,
        callsEnabled: Boolean,
    ): Boolean = callsEnabled && isIncomingCall(packageName, categoryCall, ongoing)

    fun isIncomingCall(packageName: String, categoryCall: Boolean, ongoing: Boolean): Boolean {
        if (isCellularDialer(packageName)) return false
        if (categoryCall) return true
        return ongoing && MessageChannel.isMessaging(packageName)
    }

    fun isCellularDialer(packageName: String): Boolean {
        val pkg = packageName.lowercase()
        return pkg.contains("dialer") ||
            pkg.contains("incallui") ||
            pkg.contains("telecom") ||
            pkg.endsWith(".phone")
    }

    fun callerName(title: String): String? {
        val t = title.trim()
        if (t.isEmpty() || isStatusPhrase(t)) return null
        return t.take(80)
    }

    fun event(
        settings: AppSettings,
        title: String,
        people: String,
        appLabel: String,
        looping: Boolean = true,
        repeatCount: Int? = null,
    ): SpokenEvent? = CallChannel.incoming(
        settings,
        people,
        callerName(title),
        appLabel = appLabel,
        looping = looping,
        repeatCount = repeatCount,
    )

    internal fun isStatusPhrase(value: String): Boolean {
        val x = value.lowercase()
        return listOf(
            "incoming",
            "ringing",
            "voice call",
            "video call",
            "calling",
            "llamada",
            "appel",
            "sonnerie",
        ).any { it in x }
    }
}
