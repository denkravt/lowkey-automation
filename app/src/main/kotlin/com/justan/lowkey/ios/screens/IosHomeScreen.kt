package com.justan.lowkey.ios.screens

import com.justan.lowkey.ios.resources.IosHomeViewResource
import com.justan.shared.screens.BaseScreen
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver

/**
 * Scaffolded iOS home screen. It compiles and is tagged IOS; run it once a device and WDA signing
 * are available (see README).
 */
class IosHomeScreen(driver: AppiumDriver) : BaseScreen(driver, ScreenOs.IOS) {

    override val screenName: String = "LowKey Home (iOS)"

    override fun isDisplayed(waitTimeMillis: Long, screenshotOnFailure: Boolean): Boolean =
        locators.exists(IosHomeViewResource.screenTitle, waitTimeMillis)
}
