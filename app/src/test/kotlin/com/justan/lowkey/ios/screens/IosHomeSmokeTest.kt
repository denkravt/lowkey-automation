package com.justan.lowkey.ios.screens

import com.justan.lowkey.ios.base.IosLowKeyTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** Compiles and is tagged, but there is no iOS device in this environment yet. */
class IosHomeSmokeTest : IosLowKeyTest() {

    @Test
    @Tag("SMOKE")
    @Disabled("LOCAL-1 needs an iOS device with a signed WDA and the xcuitest driver")
    @DisplayName("iOS home screen is displayed")
    fun homeIsDisplayed() {
        val home = IosHomeScreen(driver)
        assertTrue(home.isDisplayed(), "iOS home should be visible after launch")
    }
}
