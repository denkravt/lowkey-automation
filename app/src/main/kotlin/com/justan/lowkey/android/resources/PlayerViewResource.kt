package com.justan.lowkey.android.resources

import com.justan.shared.waitstrategy.LocatorStrategy

object PlayerViewResource {
    val closeButton = LocatorStrategy.Text("\u2715")

    /** "clip 2 of 3" - must not be pinned to clip 1, or the element disappears as playback advances. */
    val clipCounter = LocatorStrategy.AndroidUiAutomator("new UiSelector().textMatches(\"(?i).*clip \\d+ of \\d+.*\")")

    val playButton = LocatorStrategy.Text("Play")
    val pauseButton = LocatorStrategy.Text("Pause")
}
