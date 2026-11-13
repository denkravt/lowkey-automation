package com.justan.lowkey.android.clips

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: Move down reorders the clip list, which is the playback and export order. */
@Tag("CLIPS")
class ReorderClipsTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("moving a clip down swaps the order in the list")
    fun moveClipDown() {
        val name = uniqueProjectName("Reorder")
        val project = createProject(name)
        try {
            var current = recordClip(project, seconds = 2)
            current = recordClip(current, seconds = 4)
            assertTrue(current.awaitClipCount(2, 60_000L), "both clips should land")
            assertNotEquals(current.clipRowText(0), current.clipRowText(1), "rows must be distinguishable")

            val second = current.clipRowText(1)
            current.moveClipDown(0)
            assertTrue(current.awaitFirstClipText(second), "the moved clip should now be first")
        } finally {
            cleanupProject(name)
        }
    }
}
