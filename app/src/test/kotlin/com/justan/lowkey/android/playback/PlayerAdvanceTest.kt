package com.justan.lowkey.android.playback

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: the player walks the clips in order instead of stopping after the first. */
@Tag("PLAYBACK")
class PlayerAdvanceTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("playback advances from clip 1 to clip 2")
    fun advancesToSecondClip() {
        val name = uniqueProjectName("Advance")
        val project = createProject(name)
        try {
            var current = recordClip(project, seconds = 2)
            current = recordClip(current, seconds = 2)
            assertTrue(current.awaitClipCount(2, 60_000L), "both clips should land")

            val player = current.playVlog()
            assertTrue(player.isDisplayed(20_000L), "player did not open")
            assertTrue(player.awaitClipNumber(2, 60_000L), "playback never reached clip 2 (${player.counterText()})")
            player.close()
        } finally {
            cleanupProject(name)
        }
    }
}
