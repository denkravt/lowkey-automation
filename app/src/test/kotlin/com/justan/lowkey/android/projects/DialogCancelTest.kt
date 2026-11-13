package com.justan.lowkey.android.projects

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: cancelling a confirmation dialog leaves the data alone. */
@Tag("PROJECTS")
class DialogCancelTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("cancelling rename, clip delete and project delete changes nothing")
    fun cancellingDialogsKeepsData() {
        val name = uniqueProjectName("Cancel")
        val project = createProject(name)
        try {
            project.rename("$name x", confirm = false)
            assertTrue(project.titleShows(name), "cancelling the rename still changed the title")

            var current = recordClip(project, seconds = 2)
            current.deleteClip(0, confirm = false)
            assertEquals(1, current.clipRowCount(), "cancelling the clip delete still removed the clip")

            current.deleteProjectCancelled()
            assertTrue(current.isDisplayed(10_000L), "cancelling the project delete left the project")
        } finally {
            cleanupProject(name)
        }
    }
}
