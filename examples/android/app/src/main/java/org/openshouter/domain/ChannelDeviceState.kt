package org.openshouter.domain

enum class ShoutChannel { NOTIFICATION, CALL, MESSAGE, TIME, BATTERY, REMINDER, CALENDAR, BLUETOOTH }

data class ChannelDeviceState(
    val device: DeviceStatePolicy = DeviceStatePolicy(),
    val stream: TtsStream = TtsStream.NOTIFICATION,
    val repeatCount: Int = 0,
    val appNameCooldownSeconds: Int = AppNameCooldown.DEFAULT_SECONDS,
) {
    fun clamp(): ChannelDeviceState = copy(
        repeatCount = repeatCount.coerceIn(0, TtsPlaybackPolicy.MAX_REPEAT_COUNT),
        appNameCooldownSeconds = AppNameCooldown.clampSeconds(appNameCooldownSeconds),
    )

    fun mergePlayback(global: TtsPlaybackPolicy): TtsPlaybackPolicy =
        global.copy(stream = stream, repeatCount = repeatCount).clamp()
}

object ChannelStates {
    fun resolve(
        map: Map<ShoutChannel, ChannelDeviceState>,
        channel: ShoutChannel,
        globalDevice: DeviceStatePolicy,
        globalPlayback: TtsPlaybackPolicy,
    ): ChannelDeviceState = map[channel]?.clamp() ?: ChannelDeviceState(
        device = globalDevice,
        stream = globalPlayback.stream,
        repeatCount = globalPlayback.repeatCount,
    )

    fun channelFor(kind: SpokenEvent.Kind): ShoutChannel = when (kind) {
        SpokenEvent.Kind.CALL -> ShoutChannel.CALL
        SpokenEvent.Kind.MESSAGE -> ShoutChannel.MESSAGE
        SpokenEvent.Kind.TIME -> ShoutChannel.TIME
        SpokenEvent.Kind.REMINDER -> ShoutChannel.REMINDER
        SpokenEvent.Kind.POWER -> ShoutChannel.BATTERY
        SpokenEvent.Kind.CALENDAR -> ShoutChannel.CALENDAR
        SpokenEvent.Kind.BLUETOOTH -> ShoutChannel.BLUETOOTH
        SpokenEvent.Kind.NOTIFICATION, SpokenEvent.Kind.GEO -> ShoutChannel.NOTIFICATION
    }

    fun allowSilentVibrate(settings: AppSettings, kind: SpokenEvent.Kind): Boolean =
        settings.deviceState.allowSilentVibrate &&
            resolve(settings.channelStates, channelFor(kind), settings.deviceState, settings.ttsPlayback)
                .device.allowSilentVibrate

    fun allowPlaybackWhenSilent(settings: AppSettings, kind: SpokenEvent.Kind): Boolean =
        kind == SpokenEvent.Kind.CALL || allowSilentVibrate(settings, kind)

    fun spoken(
        settings: AppSettings,
        channel: ShoutChannel,
        kind: SpokenEvent.Kind,
        utterance: String,
        looping: Boolean = false,
        repeatCount: Int? = null,
    ): SpokenEvent {
        val state = resolve(settings.channelStates, channel, settings.deviceState, settings.ttsPlayback)
        return SpokenEvent(kind, utterance, looping, repeatCount ?: state.repeatCount, state.stream)
    }

    fun parse(stored: Set<String>): Map<ShoutChannel, ChannelDeviceState> =
        stored.mapNotNull { row ->
            val parts = row.split('|')
            val channel = runCatching { ShoutChannel.valueOf(parts.first()) }.getOrNull()
                ?: return@mapNotNull null
            val fields = parts.drop(1).mapNotNull { cell ->
                val idx = cell.indexOf('=')
                if (idx <= 0) null else cell.substring(0, idx) to cell.substring(idx + 1)
            }.toMap()
            channel to ChannelDeviceState(
                device = DeviceStatePolicy(
                    allowScreenOn = flag(fields, "so", true),
                    allowScreenOff = flag(fields, "sf", true),
                    allowHeadsetOn = flag(fields, "ho", true),
                    allowHeadsetOff = flag(fields, "hf", true),
                    allowSilentVibrate = flag(fields, "sv", false),
                    allowInCall = flag(fields, "ic", false),
                ),
                stream = runCatching {
                    TtsStream.valueOf(fields["st"] ?: TtsStream.NOTIFICATION.name)
                }.getOrDefault(TtsStream.NOTIFICATION),
                repeatCount = fields["rc"]?.toIntOrNull() ?: 0,
                appNameCooldownSeconds = fields["ac"]?.toIntOrNull()
                    ?: AppNameCooldown.DEFAULT_SECONDS,
            ).clamp()
        }.toMap()

    fun encode(map: Map<ShoutChannel, ChannelDeviceState>): Set<String> =
        map.map { (channel, state) ->
            val clamped = state.clamp()
            val d = clamped.device
            listOf(
                channel.name,
                "so=${bit(d.allowScreenOn)}",
                "sf=${bit(d.allowScreenOff)}",
                "ho=${bit(d.allowHeadsetOn)}",
                "hf=${bit(d.allowHeadsetOff)}",
                "sv=${bit(d.allowSilentVibrate)}",
                "ic=${bit(d.allowInCall)}",
                "st=${clamped.stream.name}",
                "rc=${clamped.repeatCount}",
                "ac=${clamped.appNameCooldownSeconds}",
            ).joinToString("|")
        }.toSet()

    private fun flag(fields: Map<String, String>, key: String, default: Boolean): Boolean =
        when (fields[key]) {
            "1" -> true
            "0" -> false
            else -> default
        }

    private fun bit(value: Boolean): String = if (value) "1" else "0"
}
