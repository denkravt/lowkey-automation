package com.justan.lowkey.android.clips

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: the editor previews the clip and the button flips between Preview and Pause. */
@Tag("EDITOR")
class EditorPreviewTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("Preview starts playback and Pause stops it again")
    fun previewToggles() {
        val name = uniqueProjectName("Preview")
        val project = createProject(name)
        try {
            // A short take can finish before the second tap, which restarts playback instead of pausing it.
            val current = recordClip(project, seconds = 6)
            val editor = current.openClipEditor(0)
            assertTrue(editor.isDisplayed(20_000L), "editor did not open")
            assertTrue(editor.previewToggleText().contains("Preview"), "editor should start on Preview")

            editor.togglePreview()
            assertTrue(
                editor.awaitPreviewText("Pause"),
                "preview should turn into Pause while the clip plays (saw '${editor.previewToggleText()}')",
            )

            editor.togglePreview()
            assertTrue(
                editor.awaitPreviewText("Preview"),
                "tapping Pause should return the button to Preview (saw '${editor.previewToggleText()}')",
            )
        } finally {
            cleanupProject(name)
        }
    }
}
