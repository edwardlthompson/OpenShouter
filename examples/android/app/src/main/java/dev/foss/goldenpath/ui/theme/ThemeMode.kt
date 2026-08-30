package dev.foss.goldenpath.ui.theme

enum class ThemeMode {
    System,
    Light,
    Dark,
    HighContrast,
}

fun ThemeMode.next(): ThemeMode = when (this) {
    ThemeMode.System -> ThemeMode.Light
    ThemeMode.Light -> ThemeMode.Dark
    ThemeMode.Dark -> ThemeMode.HighContrast
    ThemeMode.HighContrast -> ThemeMode.System
}
