package org.openshouter.intent

import android.os.Bundle

object TaskerPlugin {
    const val ACTION_FIRE_SETTING = "com.twofortyfouram.locale.intent.action.FIRE_SETTING"
    const val EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE"
    const val EXTRA_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB"
    const val KEY_STATE = "org.openshouter.tasker.STATE"

    fun buildBundle(enabled: Boolean): Bundle =
        Bundle().apply { putBoolean(KEY_STATE, enabled) }

    fun isEnabled(bundle: Bundle?): Boolean =
        bundle?.getBoolean(KEY_STATE, true) ?: true
}
