package org.openshouter.domain

object ShakeThreshold {
    const val DEFAULT = 2.4f
    const val MIN = 1.2f
    const val MAX = 4.0f
    const val COOLDOWN_NS = 600_000_000L
    const val GRAVITY = 9.80665f

    fun clamp(value: Float): Float = value.coerceIn(MIN, MAX)

    fun gForce(x: Float, y: Float, z: Float): Float {
        val mag = kotlin.math.sqrt((x * x + y * y + z * z).toDouble())
        return (mag / GRAVITY).toFloat()
    }

    fun isShake(
        gForce: Double,
        threshold: Float,
        nowNs: Long,
        lastShakeNs: Long,
        cooldownNs: Long = COOLDOWN_NS,
    ): Boolean {
        if (gForce <= clamp(threshold)) return false
        return nowNs - lastShakeNs > cooldownNs
    }
}
