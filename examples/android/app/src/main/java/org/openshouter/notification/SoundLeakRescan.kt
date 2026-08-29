package org.openshouter.notification

object SoundLeakRescan {
    @Volatile
    var listener: OpenShouterNotificationListener? = null

    fun request() {
        listener?.rescanActive()
    }
}