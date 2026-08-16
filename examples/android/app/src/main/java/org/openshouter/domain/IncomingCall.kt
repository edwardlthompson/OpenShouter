package org.openshouter.domain

enum class CallPhase { RINGING, OFFHOOK, IDLE }

data class IncomingCallEvent(
    val number: String,
    val displayName: String?,
    val phase: CallPhase,
) {
    val spokenName: String
        get() = displayName?.takeIf { it.isNotBlank() }
            ?: ContactRule.speakableNumber(number).ifBlank { "an unknown number" }

    val shouldLoop: Boolean get() = phase == CallPhase.RINGING
    val shouldStop: Boolean get() = phase != CallPhase.RINGING
}
