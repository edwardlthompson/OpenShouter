package org.openshouter.backup

import org.openshouter.domain.ChannelDeviceState
import org.openshouter.domain.DeviceStatePolicy
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.TtsStream

object ShouterLegacyChannels {
    fun stream(raw: String?): TtsStream = when (raw?.trim()) {
        "5", "0" -> TtsStream.NOTIFICATION
        "3", "1" -> TtsStream.MEDIA
        "4", "2" -> TtsStream.ALARM
        else -> TtsStream.MEDIA
    }

    fun repeat(raw: String?): Int {
        val n = raw?.toIntOrNull() ?: return 0
        if (n == 8) return 0
        return n.coerceIn(0, 3)
    }

    fun states(prefs: Map<String, String>): Map<ShoutChannel, ChannelDeviceState> = mapOf(
        ShoutChannel.NOTIFICATION to row(prefs, "Enotifstrm", "Enotifrptcnt", "Ennotiscrnoffonly", "Ennotifnvohdphonly", "Ennotionsilentonly"),
        ShoutChannel.CALL to row(prefs, "Ecllerstrm", "Ecllerrptcnt", headsetKey = "Encnvohdphonly", silentKey = "Encnonsilentonly"),
        ShoutChannel.MESSAGE to row(prefs, "Emsgerstrm", "Emsgerrptcnt", "Enmsgcrnoffonly", "Enmsonhesetonly", "Enmsgsilentonly"),
        ShoutChannel.TIME to row(prefs, "Etimestrm", "Etimerptcnt", "Entsmcrnoffonly", "Entsmonhesetonly", "Entsmilentonly"),
        ShoutChannel.BATTERY to row(prefs, "Ebattestrm", "Ebatterptcnt", "Enbsmcrnoffonly", "Enbsmonhesetonly", "Enbsmilentonly"),
        ShoutChannel.REMINDER to row(prefs, "Eremastrm", "Eremarptcnt", "Enrmsmcrnoffonly", "Enrmsmonhesetonly", "Enrmsmilentonly"),
    )

    private fun row(
        prefs: Map<String, String>,
        streamKey: String,
        repeatKey: String,
        screenOffKey: String? = null,
        headsetKey: String? = null,
        silentKey: String? = null,
    ): ChannelDeviceState {
        val screenOff = screenOffKey?.let { flag(prefs, it) } ?: false
        val headset = headsetKey?.let { flag(prefs, it) } ?: false
        return ChannelDeviceState(
            device = DeviceStatePolicy(
                allowScreenOn = !screenOff,
                allowHeadsetOff = !headset,
                allowSilentVibrate = silentKey?.let { flag(prefs, it) } ?: false,
            ),
            stream = stream(prefs[streamKey]),
            repeatCount = repeat(prefs[repeatKey]),
        )
    }

    private fun flag(p: Map<String, String>, key: String): Boolean =
        ShouterLegacyParse.shoutEnabled(p[key].orEmpty())
}
