package com.justan.lowkey.android.resources

import com.justan.shared.waitstrategy.LocatorStrategy

/** Home screen ids. The app exposes these through Compose semantics, not test-only tags. */
object HomeViewResource {
    val screenTitle = LocatorStrategy.Text("LowKey")
    val currentProjectCard = LocatorStrategy.Text("CURRENT PROJECT")
    val projectRows = LocatorStrategy.XPath("//*[starts-with(@content-desc,'project-row:')]")
    val newProject = LocatorStrategy.Text("+ New")
    val quickLowKey = LocatorStrategy.Text("\u25CF Low-key")
    val quickNormal = LocatorStrategy.Text("Normal")
    val settingsRow = LocatorStrategy.Text("Settings")
    val nameField = LocatorStrategy.ClassName("android.widget.EditText")
    val createButton = LocatorStrategy.Text("Create")
    val cancelButton = LocatorStrategy.Text("Cancel")
    val currentProjectSummary = LocatorStrategy.TextContains("CURRENT PROJECT")

    fun projectRow(name: String): LocatorStrategy = LocatorStrategy.AccessibilityId("project-row:$name")

    fun currentProjectName(name: String): LocatorStrategy = LocatorStrategy.Text(name)
}
