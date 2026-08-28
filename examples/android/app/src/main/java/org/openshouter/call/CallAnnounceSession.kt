package org.openshouter.call

enum class CallAnnounceAction { ANNOUNCE, IGNORE, INTERRUPT }

class CallAnnounceSession {
    @Volatile private var activeKey: String? = null
    @Volatile private var announced: Boolean = false

    fun key(packageName: String, notificationKey: String): String {
        val pkg = packageName.trim()
        val nk = notificationKey.trim()
        return if (nk.isEmpty()) pkg else "$pkg|$nk"
    }

    fun decide(packageName: String, notificationKey: String, phase: VoipCallPhase): CallAnnounceAction {
        val k = key(packageName, notificationKey)
        if (k.isEmpty()) return CallAnnounceAction.IGNORE
        return when (phase) {
            VoipCallPhase.IN_CALL, VoipCallPhase.ENDED -> {
                activeKey = null
                announced = false
                CallAnnounceAction.INTERRUPT
            }
            VoipCallPhase.INCOMING -> {
                if (announced && activeKey == k) CallAnnounceAction.IGNORE
                else {
                    activeKey = k
                    announced = true
                    CallAnnounceAction.ANNOUNCE
                }
            }
        }
    }

    fun onRemoved(packageName: String, notificationKey: String): CallAnnounceAction {
        if (activeKey == null) return CallAnnounceAction.IGNORE
        val k = key(packageName, notificationKey)
        if (k.isEmpty() || activeKey != k) return CallAnnounceAction.IGNORE
        activeKey = null
        announced = false
        return CallAnnounceAction.INTERRUPT
    }
}
