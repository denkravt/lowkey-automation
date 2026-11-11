package com.justan.shared.appium

import com.justan.shared.logging.Log
import java.util.concurrent.TimeUnit

/**
 * Fails fast when the installed Appium driver is older than the framework expects - a stale
 * uiautomator2/xcuitest driver produces session errors that look like app bugs.
 */
object DriverVersionCheck {

    private const val MIN_UIAUTOMATOR2 = "4.0.0"
    private const val MIN_XCUITEST = "7.0.0"

    fun requireMinimum(driverName: String, minimum: String = minFor(driverName)) {
        val installed = installedVersion(driverName)
        if (installed == null) {
            Log.warn("Could not read the installed $driverName driver version - continuing")
            return
        }
        check(compare(installed, minimum) >= 0) {
            "Appium driver $driverName $installed is older than the required $minimum. " +
                "Run: appium driver update $driverName"
        }
        Log.info("Appium driver $driverName $installed (>= $minimum)")
    }

    fun installedVersion(driverName: String): String? = runCatching {
        val process = ProcessBuilder("appium", "driver", "list", "--installed")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor(60, TimeUnit.SECONDS)
        Regex("""${Regex.escape(driverName)}@(\d+\.\d+\.\d+)""").find(output)?.groupValues?.get(1)
    }.getOrNull()

    private fun minFor(driverName: String): String =
        if (driverName == "xcuitest") MIN_XCUITEST else MIN_UIAUTOMATOR2

    /** Numeric semver comparison without pulling in a dependency. */
    fun compare(left: String, right: String): Int {
        val a = left.split('.').map { it.toIntOrNull() ?: 0 }
        val b = right.split('.').map { it.toIntOrNull() ?: 0 }
        for (index in 0 until maxOf(a.size, b.size)) {
            val diff = (a.getOrNull(index) ?: 0) - (b.getOrNull(index) ?: 0)
            if (diff != 0) return diff
        }
        return 0
    }
}
