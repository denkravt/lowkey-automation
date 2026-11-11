package com.justan.lowkey.android.camera

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.shared.appium.AppiumConfig
import com.justan.shared.waitstrategy.PollingWaitStrategy
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: camera sub-controls - mode label, lens flip, pause/resume and close. */
@Tag("CAMERA")
class CameraControlsTest : AndroidLowKeyTest() {

    private val polling = PollingWaitStrategy(pollIntervalMs = AppiumConfig.load().pollIntervalMs)

    @Test
    @Tag("T0")
    @DisplayName("camera exposes the low-key mode, flips lenses and pauses a take")
    fun cameraControls() {
        val name = uniqueProjectName("Camera")
        val project = createProject(name)
        try {
            val camera = project.startRecording()
            assertTrue(camera.isDisplayed(), "camera should open")
            assertTrue(
                camera.modeLabel().contains("LOW-KEY"),
                "the project record tile opens low-key mode, got '${camera.modeLabel()}'",
            )

            val flipped = camera.flipCamera()
            assertTrue(flipped.isDisplayed(), "flipping lenses should keep the camera usable")

            flipped.startRecording().awaitRecordedSeconds(2)
            flipped.pause()
            val pausedAt = flipped.elapsedSeconds()

            // While paused the clock must not advance.
            val advanced = polling.waitUntil("clock advances while paused", 4_000L) {
                flipped.elapsedSeconds() > pausedAt
            }
            assertTrue(!advanced, "the timer should freeze while paused (was ${pausedAt}s)")

            flipped.resume()
            assertTrue(
                polling.waitUntil("clock advances after resume", 10_000L) {
                    flipped.elapsedSeconds() > pausedAt
                },
                "the timer should run again after resume",
            )

            flipped.stopRecording()
            assertTrue(camera.close().isDisplayed(), "closing the camera returns to the project")
        } finally {
            cleanupProject(name)
        }
    }
}
