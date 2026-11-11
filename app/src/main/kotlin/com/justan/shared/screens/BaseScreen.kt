package com.justan.shared.screens

import com.justan.shared.appium.AppiumConfig
import com.justan.shared.helpers.Locators
import com.justan.shared.helpers.PageActionHelper
import com.justan.shared.logging.Log
import com.justan.shared.waitstrategy.ScreenOs
import io.appium.java_client.AppiumDriver
import java.io.File

/**
 * Every screen exposes the platform driver, a locator helper and an isDisplayed check.
 * Screens navigate and assert preconditions only - business assertions live in tests.
 */
abstract class BaseScreen(
    protected val driver: AppiumDriver,
    val screenOs: ScreenOs,
) {
    abstract val screenName: String

    protected val config: AppiumConfig = AppiumConfig.load()

    val log: (String) -> Unit = { message -> Log.info("[$screenName] $message") }

    val locators: Locators = Locators(
        driver = driver,
        os = screenOs,
        defaultTimeoutMs = config.locatorTimeoutMs,
        onFailure = { strategy, timeout -> captureOnFailure(strategy.description, timeout) },
    )

    val actions: PageActionHelper = PageActionHelper(driver)

    /** True when this screen owns the foreground. Captures a screenshot before returning false. */
    abstract fun isDisplayed(waitTimeMillis: Long = config.locatorTimeoutMs, screenshotOnFailure: Boolean = true): Boolean

    protected fun captureOnFailure(reason: String, timeoutMs: Long) {
        val target = com.justan.shared.config.Paths
            .resolve("build/artifacts/failures/${screenName.replace(' ', '_')}-${System.currentTimeMillis()}.png")
            .toFile()
        actions.screenshot(target)?.let { Log.warn("[$screenName] screenshot after '$reason' timeout (${timeoutMs}ms): $it") }
    }
}
