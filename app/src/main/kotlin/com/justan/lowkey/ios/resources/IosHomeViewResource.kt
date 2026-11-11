package com.justan.lowkey.ios.resources

import com.justan.shared.waitstrategy.LocatorStrategy

object IosHomeViewResource {
    val screenTitle = LocatorStrategy.AccessibilityId("LowKey")
    val projectRows = LocatorStrategy.AccessibilityId("project-row")
}
