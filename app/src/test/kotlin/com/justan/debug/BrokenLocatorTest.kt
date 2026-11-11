package com.justan.debug

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.screens.HomeScreen
import com.justan.shared.screens.BaseScreen
import com.justan.shared.waitstrategy.LocatorStrategy
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * Deliberately broken: proves a missing element produces a failure screenshot plus retained logcat
 * and recording. Tagged DEBUG so no suite picks it up.
 */
@Tag("DEBUG")
class BrokenLocatorTest : AndroidLowKeyTest() {

    @Test
    @DisplayName("a locator that cannot exist fails with evidence")
    fun missingElement() {
        val screen = object : BaseScreen(driver, ScreenOs.ANDROID) {
            override val screenName: String = "Broken locator probe"
            override fun isDisplayed(waitTimeMillis: Long, screenshotOnFailure: Boolean): Boolean =
                locators.exists(LocatorStrategy.AccessibilityId("this-id-does-not-exist"), waitTimeMillis)
        }
        assertTrue(screen.isDisplayed(waitTimeMillis = 2_000), "this assertion is supposed to fail")
    }
}
