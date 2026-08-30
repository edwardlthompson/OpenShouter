package org.openshouter.ui.channel

object FormatPreview {
    fun render(
        format: String,
        sampleApp: String = "OpenShouter",
        sampleSender: String = "Alice",
        sampleText: String = "Meeting starts in 5 minutes",
        sampleTime: String = "10:30 AM",
    ): String = format
        .replace("%app", sampleApp)
        .replace("%name", sampleSender)
        .replace("%text", sampleText)
        .replace("%time", sampleTime)
        .trim()
}
