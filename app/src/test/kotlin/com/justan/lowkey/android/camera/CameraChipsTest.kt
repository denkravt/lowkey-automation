package com.justan.lowkey.android.camera

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: the torch, blackout and lens chips toggle without tearing down the preview. */
@Tag("CAMERA")
class CameraChipsTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("torch, blackout and lens chips can be toggled without losing the camera")
    fun chipsKeepTheCameraAlive() {
        val name = uniqueProjectName("Chips")
        val project = createProject(name)
        try {
            val camera = project.startRecording()
            assertTrue(camera.isDisplayed(30_000L), "camera did not open")

            camera.toggleTorch()
            assertTrue(camera.isDisplayed(10_000L), "camera lost the preview after enabling the torch")
            camera.toggleTorch()
            assertTrue(camera.isDisplayed(10_000L), "camera lost the preview after disabling the torch")

            camera.toggleBlackout()
            assertTrue(camera.isDisplayed(10_000L), "camera lost the preview after blackout")
            camera.toggleBlackout()
            assertTrue(camera.isDisplayed(10_000L), "camera lost the preview after leaving blackout")

            val lenses = camera.lensChipCount()
            if (lenses > 1) {
                camera.tapLensChip(1)
                assertTrue(camera.isDisplayed(10_000L), "camera lost the preview after switching lens")
            }
            camera.close()
        } finally {
            cleanupProject(name)
        }
    }
}
