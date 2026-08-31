package org.openshouter.ui.astro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.foss.goldenpath.R
import java.util.Locale

@Composable
fun ClockTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AstroNumberWheel(
                    value = hour,
                    range = (0..23).toList(),
                    label = stringResource(R.string.astro_time_hour),
                    onValueChange = { newHour -> onTimeChange(newHour, minute) }
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                AstroNumberWheel(
                    value = minute,
                    range = (0..59).toList(),
                    label = stringResource(R.string.astro_time_minute),
                    onValueChange = { newMinute -> onTimeChange(hour, newMinute) }
                )
            }
        }
    }
}
