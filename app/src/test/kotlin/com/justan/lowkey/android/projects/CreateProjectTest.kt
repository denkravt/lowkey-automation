package com.justan.lowkey.android.projects

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.screens.HomeScreen
import com.justan.lowkey.android.screens.ProjectScreen
import com.justan.shared.waitstrategy.LocatorStrategy
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: creating a project from home opens an empty, usable project. */
@Tag("PROJECTS")
class CreateProjectTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("a new project is listed, opens empty and can be deleted again")
    fun createProject() {
        val name = uniqueProjectName("New")
        val project = createProject(name)
        try {
            assertTrue(project.isDisplayed(), "the freshly created project should open")
            assertTrue(project.locators.exists(LocatorStrategy.Text("No shots yet")), "a new project starts empty")
            assertTrue(project.locators.exists(LocatorStrategy.TextContains("Record into this project")), "record tile present")
        } finally {
            val home = deleteProject(project)
            assertTrue(home.isDisplayed(), "deleting returns to home")
            assertTrue(!home.projectExists(name, 2_000L), "the deleted project should disappear from the list")
        }
    }
}
