package com.justan.lowkey.android.playback

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.screens.ProjectScreen
import com.justan.lowkey.android.screens.VlogPlayerScreen
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** Smoke: a project with clips plays back in the vlog player. */
@Tag("PLAYBACK")
class PlayVlogSmokeTest : AndroidLowKeyTest() {

    @Test
    @Tag("SMOKE")
    @DisplayName("play vlog opens the player and returns to the project")
    fun playVlog() {
        val name = uniqueProjectName("Playback")
        val project = createProject(name)
        try {
            val withClip = recordClip(project, seconds = 2)
            val player: VlogPlayerScreen = withClip.playVlog()
            assertTrue(player.isDisplayed(), "the player should open from the Play vlog tile")
            assertTrue(player.counterText().contains("clip 1 of"), "player should report the clip position")

            val back = player.close()
            assertTrue(back.isDisplayed(), "closing the player returns to the project")
        } finally {
            cleanupProject(name)
        }
    }
}
