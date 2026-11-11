package com.justan.lowkey.android.recording

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.screens.HomeScreen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** Smoke: a low-key take (portrait hold, 16:9 crop) lands in the project it was shot into. */
@Tag("RECORDING")
class RecordLowKeyClipSmokeTest : AndroidLowKeyTest() {

    @Test
    @Tag("SMOKE")
    @Tag("T0")
    @DisplayName("recording a low-key clip adds it to the project")
    fun recordLowKeyClip() {
        val name = uniqueProjectName("Smoke LowKey")
        val project = createProject(name)
        try {
            assertEquals(0, project.clipCount(), "a fresh project starts with no clips")
            val after = recordClip(project, seconds = 3)
            assertEquals(1, after.clipCount(), "one take should leave exactly one clip (${after.clipSummaryText()})")
            assertTrue(after.clipSummaryText()!!.contains("clip"), "summary should describe the clip count")
        } finally {
            cleanupProject(name)
            assertTrue(HomeScreen(driver).isDisplayed(), "deleting the project should return to home")
        }
    }
}
