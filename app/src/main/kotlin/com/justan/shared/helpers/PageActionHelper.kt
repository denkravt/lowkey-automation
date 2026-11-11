package com.justan.shared.helpers

import com.justan.shared.logging.Log
import com.justan.shared.waitstrategy.RetryPolicy
import io.appium.java_client.AppiumDriver
import java.io.File
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebElement

/** Small, assertion-free actions shared by every screen. */
class PageActionHelper(
    private val driver: AppiumDriver,
    private val retryPolicy: RetryPolicy = RetryPolicy.DEFAULT,
) {
    fun tap(element: WebElement) {
        retryPolicy.execute("tap") { element.click() }
    }

    fun type(element: WebElement, value: String) {
        retryPolicy.execute("type") {
            element.click()
            element.clear()
            element.sendKeys(value)
        }
    }

    fun hideKeyboard() {
        runCatching {
            (driver as? io.appium.java_client.HidesKeyboard)?.hideKeyboard()
        }.onFailure { Log.warn("hideKeyboard failed: ${it.message}") }
    }

    /** Force-stops the app under test through adb_shell (the server runs with that permission). */
    fun forceStopApp(packageName: String) {
        runCatching {
            driver.executeScript(
                "mobile: shell",
                mapOf("command" to "am", "args" to listOf("force-stop", packageName)),
            )
        }.onFailure { Log.warn("force-stop of $packageName failed: ${it.message}") }
    }

    /** Full-device screenshot (not element-scoped) saved to [target]. */
    fun screenshot(target: File): File? = runCatching {
        target.parentFile?.mkdirs()
        val bytes = driver.getScreenshotAs(OutputType.BYTES)
        target.writeBytes(bytes)
        target
    }.onFailure { Log.error("screenshot failed", it) }.getOrNull()
}
