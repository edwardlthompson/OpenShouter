package org.openshouter.call

object CallLoopGate {
    @Volatile private var voipPackage: String? = null

    fun onVoipAnnounce(packageName: String) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        voipPackage = pkg
    }

    fun activeVoipPackage(): String? = voipPackage

    fun shouldStopForCommunicationMode(inCommunication: Boolean): Boolean =
        voipPackage != null && inCommunication

    fun cutVoip(inCommunication: Boolean, interrupt: () -> Unit): Boolean {
        if (!shouldStopForCommunicationMode(inCommunication)) return false
        clear()
        interrupt()
        return true
    }

    fun clear() {
        voipPackage = null
    }
}
