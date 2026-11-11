package com.justan.lowkey.android.clips

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.screens.HomeScreen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: "Delete all clips" empties the project but keeps it. */
@Tag("CLIPS")
class DeleteAllClipsTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("delete all clips keeps the project and clears the list")
    fun deleteAllClips() {
        val name = uniqueProjectName("ClearAll")
        val project = createProject(name)
        try {
            recordClip(project, seconds = 2)
            val cleared = project.deleteAllClips()
            assertTrue(cleared.awaitClipCount(0, 30_000L), "all clips should be gone")
            assertEquals(0, cleared.clipCount())
            assertTrue(cleared.isDisplayed(), "the project screen should stay open after clearing")

            val home: HomeScreen = cleared.deleteProject()
            assertTrue(!home.projectExists(name, 2_000L), "cleanup removed the project")
        } finally {
            cleanupProject(name)
        }
    }
}
