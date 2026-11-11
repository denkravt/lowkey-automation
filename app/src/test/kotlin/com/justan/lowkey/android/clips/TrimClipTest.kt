package com.justan.lowkey.android.clips

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: the trim editor cuts a clip down and the project summary follows. */
@Tag("CLIPS")
class TrimClipTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("trimming a clip shortens the project runtime")
    fun trimClip() {
        val name = uniqueProjectName("Trim")
        val project = createProject(name)
        try {
            val withClip = recordClip(project, seconds = 4)
            val before = withClip.clipSummaryText() ?: ""
            val beforeSeconds = withClip.summarySeconds()
            assertTrue(beforeSeconds >= 3, "expected a ~4s clip before trimming, summary was '$before'")

            val editor = withClip.openClipEditor(0)
            assertTrue(editor.isDisplayed(), "tapping a clip row should open the trim editor")

            val rangeBefore = editor.rangeLabel()
            val saved = editor.trimTo(keepFraction = 0.5).save()
            assertTrue(rangeBefore.contains("\u2192"), "the editor should show the trim range, got '$rangeBefore'")
            val after = saved.awaitSummaryChange(before, 120_000L)
            val afterSeconds = saved.summarySeconds()
            assertTrue(
                afterSeconds < beforeSeconds,
                "trimming to half should shorten the clip: '$before' -> '$after'",
            )
        } finally {
            cleanupProject(name)
        }
    }
}
