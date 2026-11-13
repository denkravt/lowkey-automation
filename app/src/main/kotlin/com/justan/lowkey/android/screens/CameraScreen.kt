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

    override fun isDisplayed(waitTimeMillis: Long, screenshotOnFailure: Boolean): Boolean {
        if (locators.exists(CameraViewResource.shutterIdle, 3_000L)) return true
        // A fresh install makes Android ask for the camera before the preview ever appears.
        val allow = com.justan.shared.waitstrategy.LocatorStrategy.Text("While using the app")
        if (locators.exists(allow, 1_500L)) locators.click(allow)
        return locators.exists(CameraViewResource.shutterIdle, waitTimeMillis)
    }

    fun startRecording(): CameraScreen {
        log("tapping the shutter")
        // The control is visible before the capture pipeline is bound; a tap in that window is lost.
        repeat(3) {
            locators.click(CameraViewResource.shutterIdle)
            if (locators.exists(CameraViewResource.shutterRecording, 3_000L)) return this
            log("shutter tap did not start a take, retrying")
        }
        check(isRecording()) { "the take did not start after three shutter taps" }
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

    fun toggleTorch(): CameraScreen {
        locators.click(CameraViewResource.torchChip)
        return this
    }

    fun toggleBlackout(): CameraScreen {
        locators.click(CameraViewResource.blackoutChip)
        return this
    }

    fun lensChipCount(): Int = locators.all(CameraViewResource.lensChips).size

    fun tapLensChip(index: Int): CameraScreen {
        locators.all(CameraViewResource.lensChips)[index].click()
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

    /** Waits until the mode pill reads e.g. "STANDARD · LANDSCAPE". */
    fun awaitModeContains(fragment: String, timeoutMs: Long = 20_000L): Boolean = runCatching {
        polling.poll("mode pill contains '$fragment'", timeoutMs) {
            val label = modeLabel()
            if (label.contains(fragment)) Unit else null
        }
    }.isSuccess || locators.exists(CameraViewResource.landscapeModeHint, 2_000L)

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
