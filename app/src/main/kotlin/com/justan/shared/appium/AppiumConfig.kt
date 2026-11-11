package com.justan.shared.appium

import com.justan.shared.config.Paths
import com.typesafe.config.ConfigFactory

data class AppiumConfig(
    val serverUrl: String,
    val startupTimeoutSeconds: Int,
    val locatorTimeoutMs: Long,
    val pollIntervalMs: Long,
    val interAppTimeoutMs: Long,
    val permissionTimeoutMs: Long,
    val killOnExit: Boolean,
    val logDir: String,
) {
    val isLocalServer: Boolean
        get() = serverUrl.contains("127.0.0.1") || serverUrl.contains("localhost")

    val port: Int
        get() = Regex(""":(\d+)""").find(serverUrl)?.groupValues?.get(1)?.toIntOrNull() ?: 4723

    companion object {
        fun load(): AppiumConfig {
            val config = ConfigFactory.load("appium.conf").getConfig("appium")
            return AppiumConfig(
                serverUrl = config.getString("serverUrl").trimEnd('/'),
                startupTimeoutSeconds = config.getInt("startupTimeoutSeconds"),
                locatorTimeoutMs = config.getConfig("locator").getLong("timeoutMs"),
                pollIntervalMs = config.getConfig("locator").getLong("pollIntervalMs"),
                interAppTimeoutMs = config.getLong("interAppTimeoutMs"),
                permissionTimeoutMs = config.getLong("permissionTimeoutMs"),
                killOnExit = config.getBoolean("killOnExit"),
                logDir = Paths.resolveString(config.getString("logDir")),
            )
        }
    }
}
