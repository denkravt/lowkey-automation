package com.justan.lowkey.android.screens

import com.justan.shared.screens.BaseScreen
import com.justan.shared.waitstrategy.LocatorStrategy
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver

/** Settings holds the recording resolution picker. */
class SettingsScreen(driver: AppiumDriver) : BaseScreen(driver, ScreenOs.ANDROID) {

    override val screenName: String = "LowKey Settings"

    override fun isDisplayed(waitTimeMillis: Long, screenshotOnFailure: Boolean): Boolean =
        locators.exists(TITLE, waitTimeMillis)

    fun selectResolution(label: String): SettingsScreen {
        locators.click(LocatorStrategy.TextContains(label))
        return this
    }

    fun back(): HomeScreen {
        locators.click(BACK)
        return HomeScreen(driver)
    }

    private companion object {
        val TITLE = LocatorStrategy.Text("Recording resolution")
        val BACK = LocatorStrategy.Text("\u2039")
    }
}
