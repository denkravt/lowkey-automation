package com.justan.lowkey.android.resources

import com.justan.shared.waitstrategy.LocatorStrategy

object EditorViewResource {
    val title = LocatorStrategy.Text("Trim clip")
    val save = LocatorStrategy.TextContains("Save trim")
    val preview = LocatorStrategy.TextContains("Preview")
    val pause = LocatorStrategy.TextContains("Pause")
    val back = LocatorStrategy.Text("\u2039")
    /** Compose exposes the range slider as one seekbar per thumb. */
    val rangeStartThumb = LocatorStrategy.AccessibilityId("Range start")
    val rangeEndThumb = LocatorStrategy.AccessibilityId("Range end")
    val hint = LocatorStrategy.TextContains("Drag the handles")
    val rangeLabel = LocatorStrategy.TextContains("\u2192")
}
