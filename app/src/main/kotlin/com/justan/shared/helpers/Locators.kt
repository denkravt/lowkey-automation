package com.justan.shared.helpers

import com.justan.shared.appium.AppiumConfig
import com.justan.shared.logging.Log
import com.justan.shared.waitstrategy.ElementWaitStrategy
import com.justan.shared.waitstrategy.LocatorStrategy
import com.justan.shared.waitstrategy.PollingWaitStrategy
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver
import org.openqa.selenium.WebElement

/**
 * Turns a LocatorStrategy into a waited element. Every lookup goes through the wait strategy, so
 * screens never call findElement directly and never sleep.
 */
class Locators(
    private val driver: AppiumDriver,
    private val os: ScreenOs,
    private val waitStrategy: ElementWaitStrategy? = null,
    private val defaultTimeoutMs: Long = AppiumConfig.load().locatorTimeoutMs,
    private val onFailure: ((LocatorStrategy, Long) -> Unit)? = null,
) {
    private val wait: ElementWaitStrategy =
        waitStrategy ?: PollingWaitStrategy(pollIntervalMs = AppiumConfig.load().pollIntervalMs)

    fun element(strategy: LocatorStrategy, timeoutMs: Long = defaultTimeoutMs): WebElement =
        findOrNull(strategy, timeoutMs) ?: run {
            onFailure?.invoke(strategy, timeoutMs)
            throw com.justan.shared.waitstrategy.WaitTimeoutException(strategy.description, timeoutMs, "not present")
        }

    fun findOrNull(strategy: LocatorStrategy, timeoutMs: Long = defaultTimeoutMs): WebElement? = runCatching {
        wait.poll(strategy.description, timeoutMs) {
            driver.findElements(strategy.toBy(os)).firstOrNull { runCatching { it.isDisplayed }.getOrDefault(false) }
                ?: driver.findElements(strategy.toBy(os)).firstOrNull()
        }
    }.onFailure {
        Log.warn("locator not found: ${strategy.description}")
    }.getOrNull()

    fun exists(strategy: LocatorStrategy, timeoutMs: Long = defaultTimeoutMs): Boolean =
        findOrNull(strategy, timeoutMs) != null

    fun click(strategy: LocatorStrategy, timeoutMs: Long = defaultTimeoutMs): WebElement {
        val element = element(strategy, timeoutMs)
        element.click()
        return element
    }

    fun textOf(strategy: LocatorStrategy, timeoutMs: Long = defaultTimeoutMs): String =
        element(strategy, timeoutMs).text ?: ""

    fun waitForGone(strategy: LocatorStrategy, timeoutMs: Long = defaultTimeoutMs): Boolean =
        runCatching { wait.poll(strategy.description, timeoutMs) { if (exists(strategy, 100L)) null else Unit } }.isSuccess

    fun all(strategy: LocatorStrategy): List<WebElement> = driver.findElements(strategy.toBy(os))
}
