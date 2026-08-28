package dev.foss.goldenpath.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.settings.SettingsLogic
import dev.foss.goldenpath.ui.theme.ThemeMode
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuDropdown
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle
import org.openshouter.ui.menu.rememberMenuScrollStore

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    updateCheckEnabled: Boolean,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollStore: MenuScrollStore = rememberMenuScrollStore(),
) {
    val themes = listOf(
        ThemeMode.System to stringResource(R.string.settings_theme_mode_system),
        ThemeMode.Light to stringResource(R.string.settings_theme_mode_light),
        ThemeMode.Dark to stringResource(R.string.settings_theme_mode_dark),
    )
    val selected = SettingsLogic.themeModeName(themeMode.name)
    MenuScaffold(stringResource(R.string.settings_title), scrollStore, "settings", onBack, modifier) {
        MenuSection(stringResource(R.string.settings_appearance)) {
            MenuBody {
                MenuDropdown(
                    label = stringResource(R.string.settings_theme_label),
                    text = themes.firstOrNull { it.first.name == selected }?.second ?: themes.first().second,
                    options = themes.map { it.first.name to it.second },
                    onSelect = { name ->
                        val mode = runCatching { ThemeMode.valueOf(SettingsLogic.themeModeName(name)) }
                            .getOrDefault(ThemeMode.System)
                        onThemeModeSelect(mode)
                    },
                )
            }
            MenuToggle(
                label = stringResource(R.string.settings_update_check_label),
                checked = updateCheckEnabled,
                onChange = onUpdateCheckChange,
                showDivider = true,
            )
        }
    }
}
