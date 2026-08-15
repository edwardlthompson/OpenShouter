package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactRuleTest {
    @Test
    fun nickBeatsContactAndBlacklistSkips() {
        val rule = ContactRule.parse(setOf("nick:+15551212=Pat", "block:5550001111"))
        assertEquals("Pat", rule.display("+1 (555) 1212", "Alice"))
        assertEquals("Alice", rule.display("5559990000", "Alice"))
        assertEquals(ContactRule.UNKNOWN, rule.display("", ""))
        assertTrue(rule.isBlocked("555-000-1111"))
        assertFalse(rule.isBlocked("5551212"))
        assertFalse(rule.toString().contains("555"))
    }
}

class ChannelDeviceStateTest {
    @Test
    fun roundTripAndInheritGlobal() {
        val state = ChannelDeviceState(
            device = DeviceStatePolicy(allowInCall = true, allowScreenOn = false),
            stream = TtsStream.ALARM,
            repeatCount = 9,
        )
        val encoded = ChannelStates.encode(mapOf(ShoutChannel.CALL to state))
        val parsed = ChannelStates.parse(encoded)[ShoutChannel.CALL]!!
        assertEquals(TtsStream.ALARM, parsed.stream)
        assertEquals(TtsPlaybackPolicy.MAX_REPEAT_COUNT, parsed.repeatCount)
        assertTrue(parsed.device.allowInCall)
        assertFalse(parsed.device.allowScreenOn)
        val inherited = ChannelStates.resolve(
            emptyMap(),
            ShoutChannel.TIME,
            DeviceStatePolicy(allowHeadsetOn = false),
            TtsPlaybackPolicy(stream = TtsStream.MEDIA, repeatCount = 2),
        )
        assertEquals(TtsStream.MEDIA, inherited.stream)
        assertEquals(2, inherited.repeatCount)
    }
}

class AppOverrideMergeTest {
    @Test
    fun nullFieldsInheritAndRichRoundTrip() {
        val globalPlay = TtsPlaybackPolicy(stream = TtsStream.NOTIFICATION, delaySeconds = 3)
        val globalNote = NotificationPolicy(ignoreEmpty = true, ignoreGroup = false)
        val row = AppOverride("pkg", "%text", speakBody = false, stream = TtsStream.MEDIA)
        assertEquals("%text", row.mergeFormat("%app"))
        assertEquals(TtsStream.MEDIA, row.mergePlayback(globalPlay).stream)
        assertEquals(3, row.mergePlayback(globalPlay).delaySeconds)
        assertFalse(row.mergeNotification(globalNote).ignoreGroup)
        assertTrue(row.mergeNotification(globalNote).ignoreEmpty)
        assertFalse(row.speakBody(true))
        val stored = AppOverrides.encodeFull(mapOf("pkg" to row))
        val parsed = AppOverrides.parseFull(stored)["pkg"]!!
        assertEquals("%text", parsed.format)
        assertEquals(TtsStream.MEDIA, parsed.stream)
        assertEquals(mapOf("a" to "%app"), AppOverrides.parse(setOf("a=%app")))
    }
}

class BatterySituationTest {
    @Test
    fun customPhraseAndDisabledKind() {
        val phrases = BatteryPhrases(
            enabled = setOf(BatterySituation.LOW),
            low = "Low%level",
        )
        assertEquals("Low, 12 percent", phrases.spoken(PowerEvent(PowerKind.LOW, 12)))
        assertEquals("", phrases.spoken(PowerEvent(PowerKind.CONNECTED, null)))
        assertEquals("Battery charged.", BatteryPhrases.render(BatteryPhrases.DEFAULT_FULL, null))
    }
}

class ReminderAndChannelFormatTest {
    @Test
    fun reminderContractAndFormatTokens() {
        assertTrue(ReminderContract.validId(1))
        assertFalse(ReminderContract.validId(0))
        assertEquals(7105, ReminderContract.requestCode(5))
        assertEquals("Incoming call from Pat", TtsFormat.call("", "Pat", "555"))
        assertEquals("Sam: hi", TtsFormat.message("%name: %text", "Sam", "hi"))
        assertEquals("Message from Sam: hi", TtsFormat.message("", "Sam", "hi"))
        assertEquals("The time is 3:00", TtsFormat.time("", "3:00"))
        assertEquals(SpokenEvent.Kind.REMINDER, SpokenEvent.Kind.valueOf("REMINDER"))
        assertEquals(SpokenEvent.Kind.MESSAGE, SpokenEvent.Kind.valueOf("MESSAGE"))
    }
}
