package com.justan.shared.waitstrategy

import io.appium.java_client.AppiumBy
import org.openqa.selenium.By

/**
 * A locator plus the way it reads in a report. Preferred order: accessibility id, resource id,
 * then text - XPath only when nothing else uniquely identifies the element.
 */
sealed class LocatorStrategy(val description: String) {
    abstract fun toBy(os: ScreenOs): By

    class Id(val resourceId: String) : LocatorStrategy("id: $resourceId") {
        override fun toBy(os: ScreenOs): By = AppiumBy.id(resourceId)
    }

    class AccessibilityId(val value: String) : LocatorStrategy("accessibility: $value") {
        override fun toBy(os: ScreenOs): By = AppiumBy.accessibilityId(value)
    }

    class XPath(val expression: String) : LocatorStrategy("xpath: $expression") {
        override fun toBy(os: ScreenOs): By = AppiumBy.xpath(expression)
    }

    class Text(val value: String) : LocatorStrategy("text: $value") {
        override fun toBy(os: ScreenOs): By = when (os) {
            ScreenOs.ANDROID -> AppiumBy.androidUIAutomator("new UiSelector().text(\"$value\")")
            ScreenOs.IOS -> AppiumBy.iOSNsPredicateString("label == \"$value\" OR name == \"$value\"")
        }
    }

    class TextContains(val value: String) : LocatorStrategy("text contains: $value") {
        override fun toBy(os: ScreenOs): By = when (os) {
            ScreenOs.ANDROID -> AppiumBy.androidUIAutomator("new UiSelector().textContains(\"$value\")")
            ScreenOs.IOS -> AppiumBy.iOSNsPredicateString("label CONTAINS \"$value\"")
        }
    }

    class ClassName(val className: String) : LocatorStrategy("class: $className") {
        override fun toBy(os: ScreenOs): By = By.className(className)
    }

    class AndroidUiAutomator(val selector: String) : LocatorStrategy("uiautomator: $selector") {
        override fun toBy(os: ScreenOs): By = AppiumBy.androidUIAutomator(selector)
    }

    class IosPredicate(val predicate: String) : LocatorStrategy("predicate: $predicate") {
        override fun toBy(os: ScreenOs): By = AppiumBy.iOSNsPredicateString(predicate)
    }
}
