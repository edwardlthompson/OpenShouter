package org.openshouter.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuDropdown
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@Composable
fun CalendarShoutScreen(
    enabled: Boolean,
    lookaheadMinutes: Int,
    onEnabled: (Boolean) -> Unit,
    onLookahead: (Int) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val choices = CalendarShout.MINUTE_CHOICES.map { minutes ->
        minutes.toString() to stringResource(R.string.calendar_lookahead_min, minutes)
    }
    MenuScaffold(stringResource(R.string.nav_calendar), scrollStore, "calendar", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            MenuToggle(stringResource(R.string.calendar_enable), enabled, onEnabled)
            MenuBody {
                Text(stringResource(R.string.calendar_help), style = MaterialTheme.typography.bodyLarge)
                MenuDropdown(
                    label = stringResource(R.string.calendar_lookahead),
                    text = choices.firstOrNull { it.first == lookaheadMinutes.toString() }?.second
                        ?: choices.first().second,
                    options = choices,
                    onSelect = { raw ->
                        onLookahead(raw.toIntOrNull() ?: CalendarShout.MINUTE_CHOICES.first())
                    },
                )
            }
        }
    }
}
