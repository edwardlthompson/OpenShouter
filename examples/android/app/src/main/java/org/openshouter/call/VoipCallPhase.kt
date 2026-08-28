package org.openshouter.call

enum class VoipCallPhase { INCOMING, IN_CALL, ENDED }

object VoipCallPhaseLogic {
    const val TYPE_INCOMING = 1
    const val TYPE_ONGOING = 2
    const val TYPE_SCREENING = 3

    fun fromCallType(callType: Int?): VoipCallPhase? = when (callType) {
        TYPE_ONGOING -> VoipCallPhase.IN_CALL
        TYPE_INCOMING, TYPE_SCREENING -> VoipCallPhase.INCOMING
        else -> null
    }

    fun phase(categoryCall: Boolean, isOngoing: Boolean, callType: Int?): VoipCallPhase {
        fromCallType(callType)?.let { return it }
        if (categoryCall || isOngoing) return VoipCallPhase.INCOMING
        return VoipCallPhase.ENDED
    }
}
