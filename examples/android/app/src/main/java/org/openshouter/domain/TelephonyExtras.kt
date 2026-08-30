package org.openshouter.domain

data class TelephonyExtras(
    val speakHangupDuration: Boolean = false,
    val callWaitingEnabled: Boolean = true,
    val conferenceHintEnabled: Boolean = true,
    val bluetoothHfpEnabled: Boolean = true,
) {
    companion object {
        fun formatDuration(seconds: Long): String {
            if (seconds <= 0) return "0 seconds"
            val mins = seconds / 60
            val secs = seconds % 60
            return when {
                mins > 0 && secs > 0 -> "$mins minute${if (mins > 1) "s" else ""} $secs second${if (secs > 1) "s" else ""}"
                mins > 0 -> "$mins minute${if (mins > 1) "s" else ""}"
                else -> "$secs second${if (secs > 1) "s" else ""}"
            }
        }

        fun conferenceHint(participantCount: Int, primaryName: String = ""): String {
            return if (primaryName.isNotBlank() && participantCount > 1) {
                "Conference call from $primaryName with $participantCount participants"
            } else if (participantCount > 1) {
                "Conference call with $participantCount participants"
            } else if (primaryName.isNotBlank()) {
                "Conference call from $primaryName"
            } else {
                "Conference call"
            }
        }

        fun callWaiting(callerName: String): String {
            val name = callerName.trim().ifBlank { "Unknown" }
            return "Call waiting from $name"
        }
    }
}
