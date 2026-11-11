package com.justan.shared.ios

import com.justan.shared.logging.Log
import com.justan.shared.waitstrategy.RetryPolicy
import java.io.File
import java.util.concurrent.TimeUnit

data class IosDevice(val udid: String, val name: String, val iosVersion: String)

/** libimobiledevice wrappers - no XCTest, just device discovery and settings. */
object IosDeviceUtils {

    fun listDevices(): List<IosDevice> {
        val udids = run("idevice_id", "-l").output.trim().lines().filter { it.isNotBlank() }
        return udids.map { udid ->
            val info = run("ideviceinfo", "-u", udid).output
            IosDevice(
                udid = udid,
                name = info.lineValue("DeviceName") ?: "unknown",
                iosVersion = info.lineValue("ProductVersion") ?: "unknown",
            )
        }
    }

    fun resolveUdid(configuredUdid: String): String {
        val devices = listDevices()
        if (configuredUdid.isNotBlank()) {
            check(devices.any { it.udid == configuredUdid }) {
                "IOS_DEVICE_UDID=$configuredUdid not found. Connected: ${devices.joinToString { it.udid }}"
            }
            return configuredUdid
        }
        val device = devices.firstOrNull() ?: error("No iOS device connected (idevice_id -l returned nothing)")
        Log.info("Resolved iOS device ${device.name} (${device.udid}) on iOS ${device.iosVersion}")
        return device.udid
    }

    fun batteryLevel(udid: String): Int? =
        run("idevicediagnostics", "-u", udid, "ioreg", "AppleSmartBattery")
            .output.lineValue("CurrentCapacity")?.toIntOrNull()

    fun run(vararg command: String): IosCommandResult = RetryPolicy(attempts = 2, delayMs = 500).execute(
        "iOS command ${command.joinToString(" ")}",
    ) {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor(60, TimeUnit.SECONDS)
        IosCommandResult(command.joinToString(" "), process.exitValue(), output)
    }

    fun pullSyslog(udid: String, target: File, stopAfterMs: Long) {
        target.parentFile?.mkdirs()
        val process = ProcessBuilder("idevicesyslog", "-u", udid).redirectErrorStream(true).start()
        process.inputStream.bufferedReader().useLines { lines ->
            val deadline = System.currentTimeMillis() + stopAfterMs
            for (line in lines) {
                target.appendText(line + "\n")
                if (System.currentTimeMillis() > deadline) break
            }
        }
        process.destroyForcibly()
    }
}

data class IosCommandResult(val command: String, val exitCode: Int, val output: String) {
    val ok: Boolean get() = exitCode == 0
}

internal fun String.lineValue(key: String): String? =
    lines().firstOrNull { it.trimStart().startsWith("$key:") }?.substringAfter(':')?.trim()
