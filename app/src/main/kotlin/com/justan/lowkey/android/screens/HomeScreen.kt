package com.justan.lowkey.android.screens

import com.justan.lowkey.android.resources.HomeViewResource
import com.justan.shared.screens.BaseScreen
import com.justan.shared.waitstrategy.LocatorStrategy
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver

/** LowKey home: current-project card, project list, pins and the settings row. */
class HomeScreen(driver: AppiumDriver) : BaseScreen(driver, ScreenOs.ANDROID) {

    override val screenName: String = "LowKey Home"

    override fun isDisplayed(waitTimeMillis: Long, screenshotOnFailure: Boolean): Boolean =
        locators.exists(HomeViewResource.screenTitle, waitTimeMillis) &&
            locators.exists(HomeViewResource.currentProjectCard, waitTimeMillis)

    fun openFirstProject(): ProjectScreen {
        log("opening the first project")
        locators.click(HomeViewResource.projectRows)
        return ProjectScreen(driver)
    }

    fun openProject(name: String): ProjectScreen {
        log("opening project '$name'")
        locators.click(HomeViewResource.projectRow(name))
        return ProjectScreen(driver)
    }

    fun createProject(name: String): HomeScreen {
        log("creating project '$name'")
        locators.click(HomeViewResource.newProject)
        val field = locators.element(HomeViewResource.nameField)
        actions.type(field, name)
        locators.click(HomeViewResource.createButton)
        return this
    }

    fun projectExists(name: String, timeoutMs: Long = 5_000L): Boolean =
        locators.exists(LocatorStrategy.Text(name), timeoutMs)

    fun quickRecord(lowKey: Boolean): CameraScreen {
        log("opening the camera from the current-project card (lowKey=$lowKey)")
        locators.click(if (lowKey) HomeViewResource.quickLowKey else HomeViewResource.quickNormal)
        return CameraScreen(driver)
    }

    fun openSettings(): SettingsScreen {
        locators.click(HomeViewResource.settingsRow)
        return SettingsScreen(driver)
    }

    /** The "N clips · M:SS" line under the current project name. */
    fun currentProjectSummary(): String =
        locators.findOrNull(LocatorStrategy.TextContains("clip"))?.text ?: ""

    fun settingsValue(): String = locators.findOrNull(LocatorStrategy.TextContains("Settings"))?.text ?: ""
}
