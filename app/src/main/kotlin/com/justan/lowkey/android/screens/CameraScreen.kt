package com.justan.lowkey.android.screens

import com.justan.lowkey.android.resources.CameraViewResource
import com.justan.shared.appium.AppiumConfig
import com.justan.shared.screens.BaseScreen
import com.justan.shared.waitstrategy.PollingWaitStrategy
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver

/** The capture surface: shutter, REC clock, mode pill and lens controls. */
class CameraScreen(driver: AppiumDriver) : BaseScreen(driver, ScreenOs.ANDROID) {

    override val screenName: String = "LowKey Camera"

    private val polling = PollingWaitStrategy(pollIntervalMs = AppiumConfig.load().pollIntervalMs)

    override fun isDisplayed(waitTimeMillis: Long, screenshotOnFailure: Boolean): Boolean =
        locators.exists(CameraViewResource.shutterIdle, waitTimeMillis)

    fun startRecording(): CameraScreen {
        log("tapping the shutter")
        locators.click(CameraViewResource.shutterIdle)
        return this
    }

    /** Waits until the REC clock reaches [seconds] - the record duration itself is a business value. */
    fun awaitRecordedSeconds(seconds: Int, timeoutMs: Long = seconds * 1000L + 15_000L): CameraScreen {
        polling.poll("recording clock >= ${seconds}s", timeoutMs) {
            if (elapsedSeconds() >= seconds) Unit else null
        }
        return this
    }

    fun stopRecording(): CameraScreen {
        log("stopping the take")
        locators.click(CameraViewResource.shutterRecording)
        return this
    }

    fun pause(): CameraScreen {
        locators.click(CameraViewResource.pauseButton)
        return this
    }

    fun resume(): CameraScreen {
        locators.click(CameraViewResource.resumeButton)
        return this
    }

    fun flipCamera(): CameraScreen {
        locators.click(CameraViewResource.flipCamera)
        return this
    }

    /** Mode pill text: "LOW-KEY · 16:9" or "STANDARD · PORTRAIT"/"STANDARD · LANDSCAPE". */
    fun modeLabel(): String =
        locators.findOrNull(CameraViewResource.lowKeyMode, 1_500L)?.text
            ?: locators.findOrNull(CameraViewResource.standardPortraitMode, 1_500L)?.text
            ?: locators.findOrNull(CameraViewResource.standardLandscapeMode, 1_500L)?.text
            ?: ""

    fun isRecording(): Boolean = locators.exists(CameraViewResource.shutterRecording, 1_000L)

    /** Closes the camera when it was opened from a project. */
    fun close(): ProjectScreen {
        locators.click(CameraViewResource.closeButton)
        return ProjectScreen(driver)
    }

    /** Closes the camera when it was opened from the home screen. */
    fun closeToHome(): HomeScreen {
        locators.click(CameraViewResource.closeButton)
        return HomeScreen(driver)
    }

    /** Highest seconds value visible in any clock label. */
    fun elapsedSeconds(): Int = locators.all(CameraViewResource.recordingClock)
        .mapNotNull { element ->
            Regex("""(\d+):(\d{2})""").find(element.text ?: "")
                ?.let { it.groupValues[1].toInt() * 60 + it.groupValues[2].toInt() }
        }
        .maxOrNull() ?: 0
}
