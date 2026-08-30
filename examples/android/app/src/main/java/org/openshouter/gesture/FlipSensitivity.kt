package org.openshouter.gesture

import kotlin.math.abs

enum class FlipMode {
    DESK_STRICT,
    NORMAL,
    POCKET_TOLERANT,
}

object FlipSensitivity {
    fun isFaceDown(x: Float, y: Float, z: Float, mode: FlipMode = FlipMode.NORMAL): Boolean = when (mode) {
        FlipMode.DESK_STRICT -> z < -9.2f && abs(x) < 1.5f && abs(y) < 1.5f
        FlipMode.NORMAL -> z < -8.5f && abs(x) < 3.0f && abs(y) < 3.0f
        FlipMode.POCKET_TOLERANT -> z < -7.5f && abs(x) < 4.5f && abs(y) < 4.5f
    }
}
