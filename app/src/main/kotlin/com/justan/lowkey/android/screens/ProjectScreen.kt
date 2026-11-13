package com.justan.lowkey.android.screens

import com.justan.lowkey.android.resources.ProjectViewResource
import com.justan.shared.appium.AppiumConfig
import com.justan.shared.screens.BaseScreen
import com.justan.shared.waitstrategy.LocatorStrategy
import com.justan.shared.waitstrategy.PollingWaitStrategy
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver

/**
 * One vlog project: record, play, export, music, recap and the clip list. It mirrors a dense screen,
 * so the function-count rule is suppressed rather than splitting the page object artificially.
 */
@Suppress("TooManyFunctions")
class ProjectScreen(driver: AppiumDriver) : BaseScreen(driver, ScreenOs.ANDROID) {

    override val screenName: String = "LowKey Project"

    private val polling = PollingWaitStrategy(pollIntervalMs = AppiumConfig.load().pollIntervalMs)

    override fun isDisplayed(waitTimeMillis: Long, screenshotOnFailure: Boolean): Boolean =
        locators.exists(ProjectViewResource.recordTile, waitTimeMillis)

    fun startRecording(): CameraScreen {
        log("opening the camera from the project")
        locators.click(ProjectViewResource.recordTile)
        val camera = CameraScreen(driver)
        if (camera.isDisplayed(12_000L)) return camera
        log("camera did not come up, tapping the record tile again")
        locators.click(ProjectViewResource.recordTile)
        return camera
    }

    fun backToHome(): HomeScreen {
        locators.click(ProjectViewResource.backButton)
        return HomeScreen(driver)
    }

    fun playVlog(): VlogPlayerScreen {
        locators.click(ProjectViewResource.playTile)
        return VlogPlayerScreen(driver)
    }

    fun exportVlog(): ProjectScreen {
        locators.click(ProjectViewResource.exportTile)
        return this
    }

    /** Waits for the export note and returns its text ("Exported 3 clips as one video · saved..."). */
    fun awaitExportNote(timeoutMs: Long = 180_000L): String {
        val note = polling.poll("export note", timeoutMs) {
            locators.findOrNull(LocatorStrategy.TextContains("Exported"), 500L)?.text
                ?: locators.findOrNull(LocatorStrategy.TextContains("Export failed"), 500L)?.text
        }
        return note ?: ""
    }

    fun clipCount(): Int = clipCountLabel()?.substringAfter(':')?.trim()?.toIntOrNull() ?: 0

    fun clipSummaryText(): String? = locators.findOrNull(ProjectViewResource.clipCountBadge)?.text

    fun clipRowCount(): Int = locators.all(ProjectViewResource.clipRowMarker).size

    /** Total runtime shown in the summary, in seconds ("1 clip · 0:07" -> 7). */
    fun summarySeconds(): Int {
        val text = clipSummaryText() ?: return 0
        val match = Regex("""(\d+):(\d{2})$""").find(text.trim()) ?: return 0
        return match.groupValues[1].toInt() * 60 + match.groupValues[2].toInt()
    }

    /** Waits until the "N clips · M:SS" line differs from [previous] (after a trim or recap). */
    fun awaitSummaryChange(previous: String, timeoutMs: Long = 120_000L): String =
        polling.poll("clip summary to change", timeoutMs) {
            clipSummaryText()?.takeIf { it != previous }
        } ?: previous

    fun awaitClipCount(expected: Int, timeoutMs: Long): Boolean = runCatching {
        polling.poll("clip count == $expected", timeoutMs) { if (clipCount() == expected) Unit else null }
    }.isSuccess

    fun openClipEditor(index: Int = 0): ClipEditorScreen {
        log("opening the editor for clip #$index")
        locators.all(ProjectViewResource.clipRowMarker)[index].click()
        return ClipEditorScreen(driver)
    }

    /** Opens the ⋮ on a clip row. */
    fun openClipMenu(index: Int = 0): ProjectScreen {
        locators.all(ProjectViewResource.clipMenuButtons)[index].click()
        return this
    }

    fun deleteClip(index: Int = 0, confirm: Boolean = true): ProjectScreen {
        openClipMenu(index)
        locators.click(ProjectViewResource.clipMenuDelete)
        if (confirm) locators.click(ProjectViewResource.deleteClipConfirm) else cancelDialog()
        return this
    }

    /**
     * Text of a clip row as a single string (duration + timestamp). The "clip-row" marker node carries
     * no text of its own, so the labels are collected from its descendants.
     */
    fun clipRowText(index: Int): String {
        val row = locators.all(ProjectViewResource.clipRows)[index]
        return row.findElements(org.openqa.selenium.By.xpath(".//*[@text]"))
            .mapNotNull { it.text?.takeIf(String::isNotBlank) }
            .joinToString(" | ")
    }

    fun cancelDialog(): ProjectScreen {
        locators.click(ProjectViewResource.dialogCancel)
        return this
    }

    fun moveClipUp(index: Int = 1): ProjectScreen {
        openClipMenu(index)
        locators.click(ProjectViewResource.clipMenuMoveUp)
        return this
    }

    fun moveClipDown(index: Int = 0): ProjectScreen {
        openClipMenu(index)
        locators.click(ProjectViewResource.clipMenuMoveDown)
        return this
    }

    fun recap(seconds: Int): ProjectScreen {
        locators.click(ProjectViewResource.recapChip(seconds))
        return this
    }

    /** Recap runs a transform per clip; wait until the summary stops changing. */
    fun awaitRecapFinished(timeoutMs: Long = 240_000L): Boolean = runCatching {
        polling.poll("recap finished", timeoutMs) {
            val hint = locators.exists(ProjectViewResource.clipsHint, 300L)
            if (hint) Unit else null
        }
    }.isSuccess

    fun openHeaderMenu(): ProjectScreen {
        locators.click(ProjectViewResource.overflowMenu)
        return this
    }

    fun deleteAllClips(): ProjectScreen {
        openHeaderMenu()
        locators.click(ProjectViewResource.deleteAllClips)
        locators.click(ProjectViewResource.deleteAllConfirm)
        return this
    }

    /** Opens the delete-project dialog and backs out of it. */
    fun deleteProjectCancelled(): ProjectScreen {
        openHeaderMenu()
        locators.click(ProjectViewResource.deleteProject)
        cancelDialog()
        return this
    }

    /** Waits until the first clip row reads [text] - used to assert a reorder took effect. */
    fun awaitFirstClipText(text: String, timeoutMs: Long = 15_000L): Boolean = runCatching {
        polling.poll("first clip row == '$text'", timeoutMs) {
            if (runCatching { clipRowText(0) }.getOrDefault("") == text) Unit else null
        }
    }.isSuccess

    fun deleteProject(): HomeScreen {
        openHeaderMenu()
        locators.click(ProjectViewResource.deleteProject)
        locators.click(ProjectViewResource.deleteClipConfirm)
        return HomeScreen(driver)
    }

    fun rename(newName: String, confirm: Boolean = true): ProjectScreen {
        log("renaming the project to '$newName'")
        locators.click(ProjectViewResource.projectTitle)
        val field = locators.element(LocatorStrategy.ClassName("android.widget.EditText"))
        actions.type(field, newName)
        if (confirm) locators.click(ProjectViewResource.saveRename) else cancelDialog()
        return this
    }

    fun titleShows(name: String, timeoutMs: Long = 5_000L): Boolean =
        locators.exists(LocatorStrategy.Text(name), timeoutMs)

    private fun clipCountLabel(): String? =
        locators.findOrNull(ProjectViewResource.clipCountBadge)?.getAttribute("content-desc")
}
