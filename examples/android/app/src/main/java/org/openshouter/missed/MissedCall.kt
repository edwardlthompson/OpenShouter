package org.openshouter.missed

import org.openshouter.domain.MissedCallPolicy
import org.openshouter.domain.TtsFormat

object MissedCall {
    fun shouldSpeak(policy: MissedCallPolicy, fromKnownContact: Boolean): Boolean =
        policy.allows(fromKnownContact)

    fun utterance(nameOrNumber: String): String = TtsFormat.missedCall(nameOrNumber)
}
