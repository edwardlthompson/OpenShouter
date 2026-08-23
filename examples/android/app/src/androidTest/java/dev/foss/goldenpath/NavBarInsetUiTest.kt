package dev.foss.goldenpath

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import dev.foss.goldenpath.ui.insets.NavigationMode
import dev.foss.goldenpath.ui.insets.readNavigationMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavBarInsetUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun setNavigationMode(mode: Int) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "settings put secure navigation_mode $mode",
        )
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsContentClearsNavigationBar_threeButton() {
        setNavigationMode(0)

        val context = composeTestRule.activity
        assertTrue(context.readNavigationMode() == NavigationMode.ThreeButton)

        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Check for updates").assertIsDisplayed()
        assertLastSettingsControlClearsNav(minClearanceFallback = 48)
    }

    @Test
    fun settingsContentClearsNavigationBar_gesture() {
        setNavigationMode(2)

        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Check for updates").assertIsDisplayed()
        assertLastSettingsControlClearsNav(minClearanceFallback = 0)
    }

    private fun assertLastSettingsControlClearsNav(minClearanceFallback: Int) {
        val decorView = composeTestRule.activity.window.decorView
        val navInset = ViewCompat.getRootWindowInsets(decorView)
            ?.getInsets(WindowInsetsCompat.Type.navigationBars())
            ?.bottom ?: 0
        val screenHeight = decorView.height
        val controlBottom = composeTestRule.onNodeWithText("Check for updates")
            .fetchSemanticsNode()
            .boundsInRoot
            .bottom
        val minClearance = if (navInset > 0) navInset else minClearanceFallback
        assertTrue(
            "Settings control bottom ($controlBottom) should be above nav bar (screen=$screenHeight inset=$navInset)",
            controlBottom <= screenHeight - minClearance + 8,
        )
    }
}
