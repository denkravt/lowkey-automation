package com.justan.lowkey.android.resources

import com.justan.shared.waitstrategy.LocatorStrategy

object PlayerViewResource {
    val closeButton = LocatorStrategy.Text("\u2715")
    val clipCounter = LocatorStrategy.TextContains("clip 1 of")
    val playButton = LocatorStrategy.Text("Play")
    val pauseButton = LocatorStrategy.Text("Pause")
}
