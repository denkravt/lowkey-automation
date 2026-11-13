package com.justan.lowkey.android.resources

import com.justan.shared.waitstrategy.LocatorStrategy

object ProjectViewResource {
    val recordTile = LocatorStrategy.TextContains("Record into this project")
    val playTile = LocatorStrategy.Text("\u25B6 Play")
    val exportTile = LocatorStrategy.TextContains("Export vlog")
    val clipsHeader = LocatorStrategy.Text("CLIPS")
    val clipsHint = LocatorStrategy.Text("Drag to reorder \u00b7 tap to trim")
    val musicHeader = LocatorStrategy.Text("MUSIC")
    val addMusic = LocatorStrategy.Text("Add track")
    val recapHeader = LocatorStrategy.Text("RECAP")
    val backButton = LocatorStrategy.Text("\u2039")
    val overflowMenu = LocatorStrategy.Text("\u22EE")

    /** Header overflow actions. */
    val deleteAllClips = LocatorStrategy.Text("Delete all clips")
    val deleteProject = LocatorStrategy.Text("Delete project")
    val deleteAllConfirm = LocatorStrategy.Text("Delete all")

    /** The clip summary line carries "clip-count:<n>" in its accessibility description. */
    val clipCountBadge = LocatorStrategy.XPath("//*[starts-with(@content-desc,'clip-count:')]")

    /** Clip rows expose their thumbnail container; the ⋮ inside a row opens the clip menu. */
    val clipRows = LocatorStrategy.XPath("//*[@clickable='true'][.//*[@content-desc='clip-row']]")
    val clipRowMarker = LocatorStrategy.AccessibilityId("clip-row")

    val projectTitle = LocatorStrategy.AccessibilityId("project-title")
    val clipMenuButtons = LocatorStrategy.AccessibilityId("clip-menu")

    val clipMenuTrim = LocatorStrategy.Text("Trim clip")
    val clipMenuMoveUp = LocatorStrategy.Text("Move up")
    val clipMenuMoveDown = LocatorStrategy.Text("Move down")
    val clipMenuDelete = LocatorStrategy.Text("Delete")

    val dialogCancel = LocatorStrategy.Text("Cancel")
    val renameDialogTitle = LocatorStrategy.Text("Rename project")
    val saveRename = LocatorStrategy.Text("Save")

    val deleteClipDialog = LocatorStrategy.Text("Delete this clip?")
    val deleteClipConfirm = LocatorStrategy.Text("Delete")

    val recapTwoSeconds = LocatorStrategy.Text("2s all")
    val recapThreeSeconds = LocatorStrategy.Text("3s all")

    fun recapChip(seconds: Int): LocatorStrategy = LocatorStrategy.Text("${seconds}s all")

    fun clipDurationSummary(text: String): LocatorStrategy = LocatorStrategy.Text(text)
}
