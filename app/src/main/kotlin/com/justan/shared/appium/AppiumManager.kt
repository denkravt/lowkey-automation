package com.justan.shared.appium

import com.justan.shared.logging.Log
import io.appium.java_client.AppiumDriver
import io.appium.java_client.service.local.AppiumDriverLocalService
import io.appium.java_client.service.local.AppiumServiceBuilder
import io.appium.java_client.service.local.flags.GeneralServerFlag
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration

/**
 * Owns the Appium server for a test run: starts one local service on first use, hands out the
 * current driver, and shuts everything down at the end unless -DskipAppiumKill=true.
 */
object AppiumManager {
    private val config: AppiumConfig = AppiumConfig.load()
    private var service: AppiumDriverLocalService? = null
    private var driver: AppiumDriver? = null
    private val skipKill = System.getProperty("skipAppiumKill")?.toBoolean() ?: false

    fun config(): AppiumConfig = config

    fun start() {
        if (service?.isRunning == true) return
        if (!config.isLocalServer) {
            require(isServerUp()) { "Appium is not reachable at ${config.serverUrl}" }
            Log.info("Using the Appium server already running at ${config.serverUrl}")
            return
        }
        if (isServerUp()) {
            Log.info("Appium already listening on ${config.serverUrl} - reusing it")
            return
        }

        val logFile = File(config.logDir).apply { mkdirs() }.resolve("appium-${System.currentTimeMillis()}.log")
        Log.info("Starting Appium on port ${config.port} (log: $logFile)")

        val builder = AppiumServiceBuilder()
            .withIPAddress("127.0.0.1")
            .usingPort(config.port)
            .withLogFile(logFile)
            .withEnvironment(serverEnvironment())
            .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
            // Appium 3 wants the driver prefix: uiautomator2 needs adb_shell for `mobile: shell`.
            .withArgument(GeneralServerFlag.ALLOW_INSECURE, "*:adb_shell")
            .withTimeout(Duration.ofSeconds(config.startupTimeoutSeconds.toLong()))

        val started = AppiumDriverLocalService.buildService(builder)
        started.start()
        service = started

        check(isServerUp()) {
            "Appium failed to start on port ${config.port}. Last log lines:\n" + logFile.tail()
        }
        Log.info("Appium is up at ${config.serverUrl}")
    }

    fun setDriver(newDriver: AppiumDriver) {
        driver?.let { runCatching { it.quit() } }
        driver = newDriver
    }

    fun driver(): AppiumDriver = driver ?: error("No Appium session - start one first")

    fun hasDriver(): Boolean = driver != null

    fun quitDriver() {
        driver?.let {
            runCatching { it.quit() }.onFailure { error -> Log.warn("Driver quit failed: ${error.message}") }
        }
        driver = null
    }

    fun stop() {
        quitDriver()
        if (skipKill) {
            Log.warn("skipAppiumKill=true - leaving the Appium server running")
            return
        }
        service?.let {
            runCatching { it.stop() }.onFailure { error -> Log.warn("Appium stop failed: ${error.message}") }
            Log.info("Appium stopped")
        }
        service = null
    }

    /**
     * The Appium subprocess needs the SDK path and the node/npm bin dir; a test JVM started by an
     * IDE often has neither, which surfaces as "Neither ANDROID_HOME nor ANDROID_SDK_ROOT".
     */
    private fun serverEnvironment(): Map<String, String> {
        val env = System.getenv().toMutableMap()
        val sdkCandidates = listOfNotNull(
            env["ANDROID_HOME"],
            env["ANDROID_SDK_ROOT"],
            System.getProperty("user.home") + "/Library/Android/sdk",
        )
        sdkCandidates.firstOrNull { File(it).resolve("platform-tools").exists() }?.let { sdk ->
            env["ANDROID_HOME"] = sdk
            env["ANDROID_SDK_ROOT"] = sdk
        }
        val npmBin = System.getProperty("user.home") + "/.npm-global/bin"
        if (File(npmBin).exists()) {
            env["PATH"] = npmBin + File.pathSeparator + (env["PATH"] ?: "")
        }
        return env
    }

    /** True when anything answers on the status endpoint. */
    fun isServerUp(): Boolean = runCatching {
        val connection = URL("${config.serverUrl}/status").openConnection() as HttpURLConnection
        connection.connectTimeout = 1500
        connection.readTimeout = 1500
        connection.requestMethod = "GET"
        val code = connection.responseCode
        connection.disconnect()
        code in 200..499
    }.getOrDefault(false)

    private fun File.tail(lines: Int = 25): String =
        runCatching { readLines().takeLast(lines).joinToString("\n") }.getOrDefault("<no log>")
}
