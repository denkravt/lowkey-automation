package com.justan.extensions

import com.justan.shared.logging.Log
import java.io.File
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver

/** Session lookup + screenshot helper shared by the failure hooks and the before-all capture. */
object FailureArtifacts {
    private var driverSupplier: (() -> WebDriver?)? = null

    fun registerDriverSupplier(supplier: () -> WebDriver?) {
        driverSupplier = supplier
    }

    fun currentDriver(): WebDriver? = runCatching { driverSupplier?.invoke() }.getOrNull()

    fun screenshot(driver: WebDriver?, target: File): File? {
        if (driver == null) return null
        return runCatching {
            target.parentFile?.mkdirs()
            val screen = driver as TakesScreenshot
            target.writeBytes(screen.getScreenshotAs(OutputType.BYTES))
            Log.warn("failure screenshot: ${target.absolutePath}")
            target
        }.onFailure { Log.warn("could not capture screenshot: ${it.message}") }.getOrNull()
    }
}
