package com.justan.lowkey.android.resources

import com.justan.shared.waitstrategy.LocatorStrategy

object CameraViewResource {
    val shutterIdle = LocatorStrategy.AccessibilityId("start-recording")
    val shutterRecording = LocatorStrategy.AccessibilityId("stop-recording")
    val closeButton = LocatorStrategy.Text("\u2715")
    val lowKeyMode = LocatorStrategy.Text("LOW-KEY \u00b7 16:9")
    val standardPortraitMode = LocatorStrategy.Text("STANDARD \u00b7 PORTRAIT")
    val standardLandscapeMode = LocatorStrategy.Text("STANDARD \u00b7 LANDSCAPE")
    val flipCamera = LocatorStrategy.Text("\u21BB")
    val pauseButton = LocatorStrategy.Text("\u2161")
    val resumeButton = LocatorStrategy.Text("\u25B6")

    val torchChip = LocatorStrategy.Text("Torch")
    val blackoutChip = LocatorStrategy.Text("Blackout")

    /** Lens chips render as "1×", "0.5×", "3×" from the camera's intrinsic zoom ratios. */
    val lensChips = LocatorStrategy.AndroidUiAutomator("new UiSelector().textMatches(\"\\d+(\\.\\d+)?×\")")

    /** Any pill that mentions the landscape orientation, whatever the surrounding wording is. */
    val landscapeModeHint = LocatorStrategy.TextContains("LANDSCAPE")

    /** The REC timer, e.g. "0:03" or "REC 0:03". */
    val recordingClock = LocatorStrategy.AndroidUiAutomator("new UiSelector().textMatches(\"\\d+:\\d{2}\")")
}
