package com.justan.lowkey.android.launch

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.resources.HomeViewResource
import com.justan.lowkey.android.screens.HomeScreen
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** Smoke: the app launches to the home screen with its primary entry points. */
@Tag("LAUNCH")
class HomeSmokeTest : AndroidLowKeyTest() {

    @Test
    @Tag("SMOKE")
    @DisplayName("app launches to home with projects, quick record and settings")
    fun homeIsUsable() {
        val home = HomeScreen(driver)
        assertTrue(home.isDisplayed(), "home should be the first screen after launch")
        assertTrue(home.locators.exists(HomeViewResource.currentProjectCard), "current-project card should render")
        assertTrue(home.locators.exists(HomeViewResource.projectRows), "at least one project row should be listed")
        assertTrue(home.locators.exists(HomeViewResource.quickLowKey), "the low-key quick-record button should be present")
        assertTrue(home.locators.exists(HomeViewResource.settingsRow), "the settings row should be present")
    }
}
