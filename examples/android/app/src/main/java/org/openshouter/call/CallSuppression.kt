package org.openshouter.call

import android.telephony.TelephonyManager

/** RINGING is the shout we want; only an answered call should mute other channels. */
object CallSuppression {
    fun blocksOtherShouts(callState: Int): Boolean =
        callState == TelephonyManager.CALL_STATE_OFFHOOK
}
