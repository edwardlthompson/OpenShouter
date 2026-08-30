package org.openshouter.intent

object PublicShoutIntent {
    const val ACTION_SHOUT_TEXT = "org.openshouter.action.SHOUT_TEXT"
    const val ACTION_MUTE_ALL = "org.openshouter.action.MUTE_ALL"
    const val ACTION_TOGGLE = "org.openshouter.action.TOGGLE_ANNOUNCER"
    const val EXTRA_TEXT = "org.openshouter.extra.TEXT"
    const val EXTRA_PRIORITY = "org.openshouter.extra.PRIORITY"

    fun sanitizeText(text: String?): String? =
        text?.trim()?.takeIf { it.isNotEmpty() }
}
