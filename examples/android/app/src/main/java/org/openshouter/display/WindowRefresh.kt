package org.openshouter.display

import android.content.Context
import android.os.Build
import android.view.Window
import android.view.WindowManager

fun Window.enableFastestSameResolutionMode() {
    val display = currentDisplay() ?: return
    val current = display.mode
    val id = fastestSameResolutionModeId(
        display.supportedModes.map {
            DisplayModeChoice(it.modeId, it.physicalWidth, it.physicalHeight, it.refreshRate)
        },
        DisplayModeChoice(current.modeId, current.physicalWidth, current.physicalHeight, current.refreshRate),
    ) ?: return
    if (attributes.preferredDisplayModeId == id) return
    attributes = attributes.apply { preferredDisplayModeId = id }
}

private fun Window.currentDisplay() = if (Build.VERSION.SDK_INT >= 30) {
    context.display
} else {
    @Suppress("DEPRECATION")
    (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
}
