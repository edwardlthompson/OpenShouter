package org.openshouter.accessibility

import dev.foss.goldenpath.ui.theme.ThemeMode
import dev.foss.goldenpath.ui.theme.next
import org.junit.Assert.assertEquals
import org.junit.Test

class AccessibilityAndThemeSprint32Test {

    @Test
    fun themeModeTransitionsIncludeHighContrast() {
        assertEquals(ThemeMode.Light, ThemeMode.System.next())
        assertEquals(ThemeMode.Dark, ThemeMode.Light.next())
        assertEquals(ThemeMode.HighContrast, ThemeMode.Dark.next())
        assertEquals(ThemeMode.System, ThemeMode.HighContrast.next())
    }
}
