package org.openshouter.domain

data class MessageChannelPolicy(
    val enabled: Boolean = false,
    val speakUnknown: Boolean = true,
    val speakBody: Boolean = true,
    val knownContactsOnly: Boolean = false,
) {
    fun allows(fromKnownContact: Boolean): Boolean {
        if (!enabled) return false
        if (knownContactsOnly && !fromKnownContact) return false
        if (!fromKnownContact && !speakUnknown) return false
        return true
    }
}

data class MissedCallPolicy(
    val enabled: Boolean = false,
    val speakUnknown: Boolean = true,
) {
    fun allows(fromKnownContact: Boolean): Boolean {
        if (!enabled) return false
        if (!fromKnownContact && !speakUnknown) return false
        return true
    }
}
