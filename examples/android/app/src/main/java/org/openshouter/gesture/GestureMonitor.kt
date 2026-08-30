package org.openshouter.gesture

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.AppSettings
import org.openshouter.domain.ShakeThreshold
import org.openshouter.tts.TtsController

@Singleton
class GestureMonitor @Inject constructor(
    @ApplicationContext context: Context,
    private val settings: SettingsRepository,
    private val tts: TtsController,
) : SensorEventListener {
    private val sensors = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var collectJob: Job? = null
    @Volatile private var cached = AppSettings()
    private var lastShakeNs = 0L

    fun start() {
        collectJob?.cancel()
        collectJob = scope.launch { settings.settings.collect { cached = it } }
        sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensors.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        collectJob?.cancel()
        sensors.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val g = ShakeThreshold.gForce(x, y, z).toDouble()
        val now = event.timestamp
        if (ShakeThreshold.isShake(g, cached.shakeThreshold, now, lastShakeNs)) {
            lastShakeNs = now
            if (cached.shakeToSilence) tts.interrupt()
        }
        val faceDown = FlipSensitivity.isFaceDown(x, y, z)
        if (faceDown && cached.flipToMute) tts.interrupt()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
