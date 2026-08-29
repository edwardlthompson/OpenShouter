package org.openshouter.silence

data class SoundInspect(
    val packageName: String,
    val channelId: String,
    val channelName: String,
    val channelSound: String?,
    val notificationSound: String?,
    val usesDefaultSound: Boolean,
    val importance: Int,
    val silentFlag: Boolean,
    val isGroup: Boolean,
    val defaultNotificationSilent: Boolean = false,
)

enum class SoundEvidence { CHANNEL_SOUND, NOTIFICATION_SOUND, DEFAULT_SOUND, OWN_AUDIO }

enum class SoundLeakAction { UPSERT, CLEAR, IGNORE }

object SoundLeakPolicy {
    const val OUR_PACKAGE = "org.openshouter"
    const val MIN_SOUND_IMPORTANCE = 3
    const val FLAG_SILENT = 0x00000100

    fun action(inspect: SoundInspect): SoundLeakAction {
        if (inspect.packageName.isBlank() || inspect.packageName == OUR_PACKAGE) return SoundLeakAction.IGNORE
        if (inspect.isGroup) return SoundLeakAction.IGNORE
        if (evidence(inspect) != null) return SoundLeakAction.UPSERT
        return if (isSilenced(inspect)) SoundLeakAction.CLEAR else SoundLeakAction.IGNORE
    }

    fun evidence(inspect: SoundInspect): SoundEvidence? {
        if (skip(inspect)) return null
        val channel = inspect.channelSound
        if (hasAudibleUri(channel) && !isDefaultSystemSound(channel)) return SoundEvidence.CHANNEL_SOUND
        if (hasAudibleUri(inspect.notificationSound) && !isDefaultSystemSound(inspect.notificationSound)) {
            return SoundEvidence.NOTIFICATION_SOUND
        }
        if (followsDefault(inspect) && !inspect.defaultNotificationSilent) return SoundEvidence.DEFAULT_SOUND
        return null
    }

    fun isSilenced(inspect: SoundInspect): Boolean {
        if (inspect.silentFlag || inspect.importance < MIN_SOUND_IMPORTANCE) return true
        if (hasAudibleUri(inspect.channelSound) && !isDefaultSystemSound(inspect.channelSound)) return false
        if (hasAudibleUri(inspect.notificationSound) && !isDefaultSystemSound(inspect.notificationSound)) {
            return false
        }
        if (followsDefault(inspect) && !inspect.defaultNotificationSilent) return false
        return true
    }

    fun followsDefault(inspect: SoundInspect): Boolean =
        inspect.usesDefaultSound ||
            isDefaultSystemSound(inspect.channelSound) ||
            isDefaultSystemSound(inspect.notificationSound)

    fun skip(inspect: SoundInspect): Boolean {
        if (inspect.packageName.isBlank() || inspect.packageName == OUR_PACKAGE) return true
        if (inspect.isGroup || inspect.silentFlag) return true
        return inspect.importance < MIN_SOUND_IMPORTANCE
    }

    fun hasAudibleUri(uri: String?): Boolean {
        if (uri.isNullOrBlank()) return false
        return !SilentWav.isSilentUri(uri)
    }

    fun isDefaultSystemSound(uri: String?): Boolean {
        val n = uri?.lowercase() ?: return false
        return n.contains("settings/system/notification_sound") ||
            n.contains("settings/system/ringtone")
    }
}
