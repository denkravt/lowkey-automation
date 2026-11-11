package com.justan.lowkey.android.projects

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: tapping the title renames the project. */
@Tag("PROJECTS")
class RenameProjectTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("the project title can be renamed and the new name sticks")
    fun renameProject() {
        val name = uniqueProjectName("Rename")
        val project = createProject(name)
        try {
            val newName = uniqueProjectName("Renamed")
            project.rename(newName)
            assertTrue(project.titleShows(newName), "header should show the new name '$newName'")
        } finally {
            cleanupProject(name)
        }
    }
}
