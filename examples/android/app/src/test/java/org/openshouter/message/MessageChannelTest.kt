package org.openshouter.message

import org.junit.Assert.assertEquals
import org.junit.Test
import org.openshouter.domain.AppSettings
import org.openshouter.domain.MessageChannelPolicy

class MessageChannelTest {
    private val settings = AppSettings(
        messageChannel = MessageChannelPolicy(enabled = true, speakBody = true),
    )

    @Test
    fun cooldownDropsAppLabelSenderAndKeepsBody() {
        val parsed = MessageChannel.parse("Messages", "sent a photo")
        assertEquals(
            "sent a photo",
            MessageChannel.utterance(settings, parsed, "Messages", "Messages", includeAppName = false),
        )
    }

    @Test
    fun firstBurstKeepsSenderThatIsAppLabel() {
        val parsed = MessageChannel.parse("Messages", "sent a photo")
        assertEquals(
            "Message from Messages: sent a photo",
            MessageChannel.utterance(settings, parsed, "Messages", "Messages", includeAppName = true),
        )
    }

    @Test
    fun contactNameIsNotTreatedAsAppLabel() {
        val parsed = MessageChannel.parse("Jane", "hi")
        assertEquals(
            "Message from Jane: hi",
            MessageChannel.utterance(settings, parsed, "Jane", "Messages", includeAppName = false),
        )
    }
}
