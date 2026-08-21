package org.openshouter.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@OptIn(ExperimentalLayoutApi::class)
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
    MenuScaffold(stringResource(R.string.nav_calendar), scrollStore, "calendar", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            MenuToggle(stringResource(R.string.calendar_enable), enabled, onEnabled)
            MenuBody {
                Text(stringResource(R.string.calendar_help), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.calendar_lookahead), style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    CalendarShout.MINUTE_CHOICES.forEach { minutes ->
                        FilterChip(
                            selected = lookaheadMinutes == minutes,
                            onClick = { onLookahead(minutes) },
                            label = { Text(stringResource(R.string.calendar_lookahead_min, minutes)) },
                        )
                    }
                }
            }
        }
    }
}
