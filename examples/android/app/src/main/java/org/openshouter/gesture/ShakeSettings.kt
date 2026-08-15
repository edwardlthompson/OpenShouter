package org.openshouter.gesture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.ShakeThreshold

private const val NUDGE = 0.2f

@Composable
fun ShakeSettings(
    threshold: Float,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.shake_threshold)
    val clamped = ShakeThreshold.clamp(threshold)
    val context = LocalContext.current
    var liveG by remember { mutableFloatStateOf(1f) }
    DisposableEffect(Unit) {
        val sensors = context.getSystemService(SensorManager::class.java)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                liveG = ShakeThreshold.gForce(event.values[0], event.values[1], event.values[2])
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensors.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensors.unregisterListener(listener) }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Slider(
            value = clamped,
            onValueChange = { onChange(ShakeThreshold.clamp(it)) },
            valueRange = ShakeThreshold.MIN..ShakeThreshold.MAX,
            modifier = Modifier.semantics { contentDescription = label },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Button(
                onClick = { onChange(ShakeThreshold.clamp(clamped - NUDGE)) },
                modifier = Modifier.semantics { contentDescription = label },
            ) {
                Text(stringResource(R.string.quiet_minus))
            }
            Text(
                stringResource(R.string.shake_live, clamped, liveG),
                style = MaterialTheme.typography.titleMedium,
            )
            Button(
                onClick = { onChange(ShakeThreshold.clamp(clamped + NUDGE)) },
                modifier = Modifier.semantics { contentDescription = label },
            ) {
                Text(stringResource(R.string.quiet_plus))
            }
        }
    }
}
