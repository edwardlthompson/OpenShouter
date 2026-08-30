package dev.foss.goldenpath.about

object UnifiedPushPing {
    const val ACTION_REGISTER = "org.unifiedpush.android.connector.REGISTER"
    const val ACTION_UNREGISTER = "org.unifiedpush.android.connector.UNREGISTER"
    const val ACTION_MESSAGE = "org.unifiedpush.android.connector.MESSAGE"

    data class PushConfig(
        val enabled: Boolean = false,
        val endpoint: String = "",
        val distributorPackage: String = "",
    )

    fun isConfigured(config: PushConfig): Boolean =
        config.enabled && config.endpoint.isNotBlank()
}
