package com.justan.lowkey.android.screens

import com.justan.lowkey.android.resources.PlayerViewResource
import com.justan.shared.screens.BaseScreen
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver

/** Sequential playback of every clip in the project. */
class VlogPlayerScreen(driver: AppiumDriver) : BaseScreen(driver, ScreenOs.ANDROID) {

    override val screenName: String = "Vlog player"

    override fun isDisplayed(waitTimeMillis: Long, screenshotOnFailure: Boolean): Boolean =
        locators.exists(PlayerViewResource.clipCounter, waitTimeMillis)

    /** Waits until the player reaches clip [number] (the vlog plays every clip in order). */
    fun awaitClipNumber(number: Int, timeoutMs: Long = 60_000L): Boolean = runCatching {
        com.justan.shared.waitstrategy.PollingWaitStrategy().poll("clip $number of", timeoutMs) {
            if (counterText().contains("clip $number of")) Unit else null
        }
    }.isSuccess

    fun counterText(): String = locators.findOrNull(PlayerViewResource.clipCounter)?.text ?: ""

    fun pause(): VlogPlayerScreen {
        locators.click(PlayerViewResource.pauseButton)
        return this
    }

    fun close(): ProjectScreen {
        locators.click(PlayerViewResource.closeButton)
        return ProjectScreen(driver)
    }
}
