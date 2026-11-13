package com.justan.lowkey.android.screens

import com.justan.lowkey.android.resources.EditorViewResource
import com.justan.shared.screens.BaseScreen
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver
import java.time.Duration
import org.openqa.selenium.interactions.Actions

/** Trim editor: range slider, preview and save. */
class ClipEditorScreen(driver: AppiumDriver) : BaseScreen(driver, ScreenOs.ANDROID) {

    override val screenName: String = "Clip editor"

    override fun isDisplayed(waitTimeMillis: Long, screenshotOnFailure: Boolean): Boolean =
        locators.exists(EditorViewResource.title, waitTimeMillis) &&
            locators.exists(EditorViewResource.save, waitTimeMillis)

    /** "Preview" or "Pause" - the button flips while the clip plays. */
    fun previewToggleText(): String =
        locators.findOrNull(EditorViewResource.pause, 800L)?.text
            ?: locators.findOrNull(EditorViewResource.preview, 2_000L)?.text
            ?: ""

    /** Waits until the toggle reads [fragment] ("Preview" or "Pause"). */
    fun awaitPreviewText(fragment: String, timeoutMs: Long = 20_000L): Boolean = runCatching {
        com.justan.shared.waitstrategy.PollingWaitStrategy().poll("preview button '$fragment'", timeoutMs) {
            if (previewToggleText().contains(fragment)) Unit else null
        }
    }.isSuccess

    fun togglePreview(): ClipEditorScreen {
        val pause = locators.findOrNull(EditorViewResource.pause, 800L)
        if (pause != null) pause.click() else locators.click(EditorViewResource.preview)
        return this
    }

    /** The "0:00 → 0:04" label under the slider. */
    fun rangeLabel(): String = locators.findOrNull(EditorViewResource.rangeLabel, 2_000L)?.text ?: ""

    /**
     * Drags the end handle left so roughly [keepFraction] of the clip survives. The drag has to start
     * inside the end thumb, otherwise Compose treats it as a track tap and nothing moves.
     */
    fun trimTo(keepFraction: Double = 0.5): ClipEditorScreen {
        val before = rangeLabel()
        for (attempt in 1..2) {
            dragEndThumbTo(keepFraction)
            if (rangeLabel() != before) return this
            log("drag attempt $attempt did not move the handle")
        }
        check(rangeLabel() != before) { "the trim range did not change (label stayed '$before')" }
        return this
    }

    /**
     * Drags the end thumb to [keepFraction] of the track. Compose renders one seekbar per thumb, so
     * the drag starts on the "Range end" node itself - a drag from elsewhere on the track is ignored.
     */
    private fun dragEndThumbTo(keepFraction: Double) {
        val startThumb = locators.element(EditorViewResource.rangeStartThumb).rect
        val endThumb = locators.element(EditorViewResource.rangeEndThumb).rect
        val trackLeft = startThumb.x + startThumb.width / 2
        val trackRight = endThumb.x + endThumb.width / 2
        val y = endThumb.y + endThumb.height / 2
        val targetX = trackLeft + ((trackRight - trackLeft) * keepFraction).toInt()

        driver.executeScript(
            "mobile: dragGesture",
            mapOf(
                "startX" to trackRight,
                "startY" to y,
                "endX" to targetX,
                "endY" to y,
            ),
        )
    }

    fun save(): ProjectScreen {
        log("saving the trim")
        locators.click(EditorViewResource.save)
        return ProjectScreen(driver)
    }

    fun back(): ProjectScreen {
        locators.click(EditorViewResource.back)
        return ProjectScreen(driver)
    }
}
