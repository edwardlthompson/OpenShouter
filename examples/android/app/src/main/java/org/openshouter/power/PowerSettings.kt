package org.openshouter.power

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.BatteryPhrases
import org.openshouter.domain.BatterySituation

@Composable
fun PowerSettings(
    phrases: BatteryPhrases,
    onChange: (BatteryPhrases) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text("Battery situations", style = MaterialTheme.typography.headlineSmall)
        SituationToggle("Low battery", BatterySituation.LOW, phrases, onChange)
        PhraseField("Low phrase", phrases.low) { onChange(phrases.copy(low = it.take(BatteryPhrases.MAX_PHRASE))) }
        SituationToggle("Battery full", BatterySituation.FULL, phrases, onChange)
        PhraseField("Full phrase", phrases.full) { onChange(phrases.copy(full = it.take(BatteryPhrases.MAX_PHRASE))) }
        SituationToggle("Power connected", BatterySituation.CONNECTED, phrases, onChange)
        PhraseField("Connected phrase", phrases.connected) {
            onChange(phrases.copy(connected = it.take(BatteryPhrases.MAX_PHRASE)))
        }
        SituationToggle("Power disconnected", BatterySituation.DISCONNECTED, phrases, onChange)
        PhraseField("Disconnected phrase", phrases.disconnected) {
            onChange(phrases.copy(disconnected = it.take(BatteryPhrases.MAX_PHRASE)))
        }
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) { Text("Close") }
    }
}

@Composable
private fun SituationToggle(
    label: String,
    situation: BatterySituation,
    phrases: BatteryPhrases,
    onChange: (BatteryPhrases) -> Unit,
) {
    val checked = situation in phrases.enabled
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { on ->
                val next = if (on) phrases.enabled + situation else phrases.enabled - situation
                onChange(phrases.copy(enabled = next))
            },
            modifier = Modifier.semantics { contentDescription = label },
        )
    }
}

@Composable
private fun PhraseField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}
