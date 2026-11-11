package com.justan.lowkey.android.clips

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.shared.testdata.TestDataManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: the per-clip ⋮ menu deletes a single clip. */
@Tag("CLIPS")
class DeleteClipTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("deleting a clip removes only that clip")
    fun deleteOneClip() {
        val name = uniqueProjectName("ClipDelete")
        val project = createProject(name)
        try {
            var current = recordClip(project, seconds = 2)
            current = current.also { it.deleteClip(0) }
            assertTrue(
                current.awaitClipCount(0, 30_000L),
                "clip count should return to zero (was ${current.clipCount()})",
            )
            assertEquals(0, current.clipCount())
        } finally {
            cleanupProject(name)
        }
    }
}
