package org.openshouter.power

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.domain.BatteryPhrases
import org.openshouter.domain.BatterySituation
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@Composable
fun PowerSettings(
    phrases: BatteryPhrases,
    onChange: (BatteryPhrases) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    MenuScaffold(stringResource(R.string.nav_power), scrollStore, "power", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            SituationToggle(R.string.power_low, BatterySituation.LOW, phrases, onChange)
            PhraseField(R.string.power_low_phrase, phrases.low) {
                onChange(phrases.copy(low = it.take(BatteryPhrases.MAX_PHRASE)))
            }
            SituationToggle(R.string.power_full, BatterySituation.FULL, phrases, onChange, true)
            PhraseField(R.string.power_full_phrase, phrases.full) {
                onChange(phrases.copy(full = it.take(BatteryPhrases.MAX_PHRASE)))
            }
            SituationToggle(R.string.power_connected, BatterySituation.CONNECTED, phrases, onChange, true)
            PhraseField(R.string.power_connected_phrase, phrases.connected) {
                onChange(phrases.copy(connected = it.take(BatteryPhrases.MAX_PHRASE)))
            }
            SituationToggle(R.string.power_disconnected, BatterySituation.DISCONNECTED, phrases, onChange, true)
            PhraseField(R.string.power_disconnected_phrase, phrases.disconnected) {
                onChange(phrases.copy(disconnected = it.take(BatteryPhrases.MAX_PHRASE)))
            }
            SituationToggle(R.string.power_level, BatterySituation.LEVEL, phrases, onChange, true)
            PhraseField(R.string.power_level_phrase, phrases.level) {
                onChange(phrases.copy(level = it.take(BatteryPhrases.MAX_PHRASE)))
            }
        }
    }
}

@Composable
private fun SituationToggle(
    labelRes: Int,
    situation: BatterySituation,
    phrases: BatteryPhrases,
    onChange: (BatteryPhrases) -> Unit,
    showDivider: Boolean = false,
) {
    val label = stringResource(labelRes)
    MenuToggle(label, situation in phrases.enabled, { on ->
        val next = if (on) phrases.enabled + situation else phrases.enabled - situation
        onChange(phrases.copy(enabled = next))
    }, showDivider)
}

@Composable
private fun PhraseField(labelRes: Int, value: String, onChange: (String) -> Unit) {
    MenuBody {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(stringResource(labelRes)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}
