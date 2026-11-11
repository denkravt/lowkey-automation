package com.justan.lowkey.android.settings

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.screens.HomeScreen
import com.justan.lowkey.android.screens.SettingsScreen
import com.justan.shared.waitstrategy.LocatorStrategy
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** Smoke: the recording-resolution setting is reachable and persists. */
@Tag("SETTINGS")
class SettingsSmokeTest : AndroidLowKeyTest() {

    @Test
    @Tag("SMOKE")
    @Tag("T0")
    @DisplayName("changing the recording resolution persists and is shown on home")
    fun resolutionSettingPersists() {
        val home = HomeScreen(driver)
        val settings: SettingsScreen = home.openSettings()
        assertTrue(settings.isDisplayed(), "settings should open from the home row")

        settings.selectResolution("720p")
        val back = settings.back()
        assertTrue(back.isDisplayed(), "back returns to home")
        assertTrue(
            back.locators.exists(LocatorStrategy.TextContains("720p"), 5_000L),
            "home should show the chosen resolution",
        )

        // Restore the default so later runs start from a known state.
        val settingsAgain = HomeScreen(driver).openSettings()
        settingsAgain.selectResolution("1080p")
        settingsAgain.back()
        assertTrue(
            HomeScreen(driver).locators.exists(LocatorStrategy.TextContains("1080p"), 5_000L),
            "1080p should be restored",
        )
    }
}
