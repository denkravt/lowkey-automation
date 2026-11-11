package com.justan.lowkey.android.projects

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: deleting a project from its header removes it and its clips. */
@Tag("PROJECTS")
class DeleteProjectTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("deleting a project removes it from the home list")
    fun deleteProject() {
        val name = uniqueProjectName("Delete")
        val project = createProject(name)
        recordClip(project, seconds = 2)

        val home = project.deleteProject()
        assertTrue(home.isDisplayed(), "deleting a project should land back on home")
        assertFalse(home.projectExists(name, 2_000L), "the deleted project must be gone from the list")
    }
}
