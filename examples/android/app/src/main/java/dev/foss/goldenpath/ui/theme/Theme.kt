package dev.foss.goldenpath.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import dev.foss.goldenpath.ui.insets.ApplySystemBarStyle

@Composable
fun GoldenPathTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark, ThemeMode.HighContrast -> true
    }
    val colorScheme = when (themeMode) {
        ThemeMode.HighContrast -> HighContrastGoldenPathColors
        ThemeMode.Dark -> DarkGoldenPathColors
        ThemeMode.Light -> LightGoldenPathColors
        ThemeMode.System -> if (systemDark) DarkGoldenPathColors else LightGoldenPathColors
    }

    ApplySystemBarStyle(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GoldenPathTypography,
        content = content,
    )
}
