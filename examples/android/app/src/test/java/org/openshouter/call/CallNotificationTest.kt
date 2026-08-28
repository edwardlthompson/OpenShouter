package org.openshouter.call

import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.domain.AppSettings
import org.openshouter.domain.MissedCallPolicy

class CallNotificationTest {
    @Test
    fun keepsOngoingVoipCalls() {
        assertTrue(CallNotification.ignorePosted("com.mail.app", ongoing = true, categoryCall = false))
        assertFalse(CallNotification.ignorePosted("com.whatsapp.w4b", ongoing = true, categoryCall = false))
        assertFalse(CallNotification.ignorePosted("com.whatsapp", ongoing = true, categoryCall = true))
        assertFalse(CallNotification.ignorePosted("com.mail.app", ongoing = false, categoryCall = false))
        assertTrue(CallNotification.ignorePosted("com.google.android.dialer", ongoing = true, categoryCall = true))
    }

    @Test
    fun routesVoipNotDialer() {
        assertTrue(
            CallNotification.routeAsCall("com.whatsapp.w4b", categoryCall = true, ongoing = false, callsEnabled = true),
        )
        assertTrue(
            CallNotification.routeAsCall("com.whatsapp", categoryCall = false, ongoing = true, callsEnabled = true),
        )
        assertTrue(
            CallNotification.routeAsCall("org.telegram.messenger", categoryCall = true, ongoing = false, callsEnabled = true),
        )
        assertFalse(
            CallNotification.routeAsCall("com.google.android.dialer", categoryCall = true, ongoing = true, callsEnabled = true),
        )
        assertFalse(
            CallNotification.routeAsCall("com.android.phone", categoryCall = true, ongoing = true, callsEnabled = true),
        )
        assertFalse(
            CallNotification.routeAsCall("com.whatsapp", categoryCall = true, ongoing = false, callsEnabled = false),
        )
        assertFalse(
            CallNotification.routeAsCall("com.whatsapp", categoryCall = false, ongoing = false, callsEnabled = true),
        )
    }

    @Test
    fun callerNameSkipsStatusPhrases() {
        assertEquals("Ada", CallNotification.callerName("Ada"))
        assertNull(CallNotification.callerName("Incoming voice call"))
        assertNull(CallNotification.callerName("Ringing"))
        assertNull(CallNotification.callerName(""))
    }

    @Test
    fun eventUsesAppWhenCallerUnknown() {
        val spoken = CallNotification.event(AppSettings(), "Incoming voice call", "", "WhatsApp")
        assertEquals("Incoming call from WhatsApp", spoken?.utterance)
        val named = CallNotification.event(AppSettings(), "Ada", "", "WhatsApp")
        assertEquals("Incoming WhatsApp call from Ada", named?.utterance)
        val blocked = CallNotification.event(
            AppSettings(missedCall = MissedCallPolicy(speakUnknown = false)),
            "Incoming voice call",
            "",
            "",
        )
        assertNull(blocked)
    }

    @Test
    fun answeredInCallDoesNotProduceLoopingEvent() {
        val session = CallAnnounceSession()
        val incoming = CallPosted.action(
            "com.whatsapp", "wa-call", categoryCall = true, isOngoing = true,
            callType = VoipCallPhaseLogic.TYPE_INCOMING, session = session,
        )
        assertEquals(CallAnnounceAction.ANNOUNCE, incoming)
        val spoken = CallPosted.eventFor(AppSettings(), "Ada", "", "WhatsApp", "com.whatsapp")
        assertFalse(spoken!!.looping)
        assertEquals(0, spoken.repeatCount)
        assertEquals(
            CallAnnounceAction.IGNORE,
            CallPosted.action(
                "com.whatsapp", "wa-call", categoryCall = true, isOngoing = true,
                callType = VoipCallPhaseLogic.TYPE_INCOMING, session = session,
            ),
        )
        assertEquals(
            CallAnnounceAction.INTERRUPT,
            CallPosted.action(
                "com.whatsapp", "wa-call", categoryCall = true, isOngoing = true,
                callType = VoipCallPhaseLogic.TYPE_ONGOING, session = session,
            ),
        )
    }
}

class CallSuppressionTest {
    @Test
    fun ringingDoesNotCountAsInCall() {
        assertFalse(CallSuppression.blocksOtherShouts(TelephonyManager.CALL_STATE_IDLE))
        assertFalse(CallSuppression.blocksOtherShouts(TelephonyManager.CALL_STATE_RINGING))
        assertTrue(CallSuppression.blocksOtherShouts(TelephonyManager.CALL_STATE_OFFHOOK))
    }
}
