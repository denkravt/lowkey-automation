package com.justan.lowkey.android.recording

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.screens.HomeScreen
import com.justan.shared.testdata.TestDataManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** Smoke: standard (non low-key) recording from the home screen lands in the current project. */
@Tag("RECORDING")
class RecordStandardClipSmokeTest : AndroidLowKeyTest() {

    @Test
    @Tag("SMOKE")
    @DisplayName("standard recording from the home card adds a clip to the current project")
    fun recordStandardClip() {
        val name = uniqueProjectName("Smoke Standard")
        var project = createProject(name)
        try {
            val before = project.clipCount()
            val home = project.backToHome()
            val camera = home.quickRecord(lowKey = false)
            assertTrue(camera.isDisplayed(), "the Normal button should open the camera")
            camera.startRecording().awaitRecordedSeconds(TestDataManager().recordSeconds()).stopRecording()
            camera.closeToHome()

            project = HomeScreen(driver).openProject(name)
            assertTrue(
                project.awaitClipCount(before + 1, TestDataManager().cropTimeoutMs()),
                "a standard take should add a clip (was $before, now ${project.clipCount()})",
            )
            assertEquals(before + 1, project.clipCount())
        } finally {
            cleanupProject(name)
        }
    }
}
