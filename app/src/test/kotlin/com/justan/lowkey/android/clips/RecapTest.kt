package com.justan.lowkey.android.clips

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: recap trims every clip to the chosen length. */
@Tag("CLIPS")
class RecapTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("recap 2s trims both clips to two seconds each")
    fun recapAllClips() {
        val name = uniqueProjectName("Recap")
        val project = createProject(name)
        try {
            var current = recordClip(project, seconds = 4)
            current = recordClip(current, seconds = 4)
            assertEquals(2, current.clipCount(), "both takes should be in the project")
            val beforeSeconds = current.summarySeconds()
            val beforeSummary = current.clipSummaryText() ?: ""
            assertTrue(beforeSeconds >= 6, "expected roughly 8s before the recap, was $beforeSummary")

            val recapped = current.recap(2)
            assertTrue(recapped.awaitRecapFinished(240_000L), "recap should finish")
            val after = recapped.awaitSummaryChange(beforeSummary, 120_000L)
            assertTrue(
                recapped.summarySeconds() <= 5,
                "two 2s clips should total about 4s, summary was '$after'",
            )
        } finally {
            cleanupProject(name)
        }
    }
}
