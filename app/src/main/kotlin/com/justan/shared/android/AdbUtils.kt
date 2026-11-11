package com.justan.shared.android

import com.justan.shared.logging.Log
import java.io.File
import java.util.concurrent.TimeUnit

data class CommandResult(val command: String, val exitCode: Int, val output: String) {
    val ok: Boolean get() = exitCode == 0

    fun requireOk(): CommandResult {
        check(ok) { "$command failed (exit $exitCode):\n$output" }
        return this
    }
}

data class AdbDevice(
    val udid: String,
    val state: String,
    val model: String?,
    val product: String?,
) {
    val isEmulator: Boolean get() = udid.startsWith("emulator-") || model?.contains("sdk_gphone") == true
}

/** Thin, testable wrapper around the adb binary. Every call can be scoped to a device. */
class AdbUtils(val udid: String? = null) {

    fun listDevices(): List<AdbDevice> = exec("devices", "-l").output.lineSequence()
        .drop(1)
        .mapNotNull { line ->
            val parts = line.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            if (parts.size < 2) return@mapNotNull null
            val extras = parts.drop(2).associate { it.substringBefore(':') to it.substringAfter(':', "") }
            AdbDevice(
                udid = parts[0],
                state = parts[1],
                model = extras["model"],
                product = extras["product"],
            )
        }
        .filter { it.state == "device" }
        .toList()

    fun firstPhysicalDevice(): AdbDevice? = listDevices().firstOrNull { !it.isEmulator }

    fun exec(vararg args: String): CommandResult {
        val command = buildList {
            add("adb")
            udid?.takeIf { it.isNotBlank() }?.let { addAll(listOf("-s", it)) }
            addAll(args)
        }
        return runCommand(command)
    }

    fun shell(command: String): CommandResult = exec("shell", command)

    fun shellOutput(command: String): String = shell(command).output.trim()

    fun install(apk: File, reinstall: Boolean = true): Boolean {
        val args = buildList {
            add("install")
            if (reinstall) add("-r")
            add("-t") // debug/test-only builds are signed with testOnly
            add(apk.absolutePath)
        }
        val result = exec(*args.toTypedArray())
        if (!result.ok) {
            Log.warn("adb install failed (${result.exitCode}): ${result.output.trim()}")
        }
        return result.ok && !result.output.contains("Failure")
    }

    fun uninstall(packageName: String): Boolean =
        exec("uninstall", packageName).output.contains("Success")

    fun isInstalled(packageName: String): Boolean =
        shell("pm list packages $packageName").output.contains(packageName)

    fun grantPermission(packageName: String, permission: String): Boolean =
        shell("pm grant $packageName $permission").ok

    fun sdkLevel(): Int = shellOutput("getprop ro.build.version.sdk").toIntOrNull() ?: 0

    fun screenSize(): Pair<Int, Int> {
        val output = shellOutput("wm size")
        val match = Regex("""(\d+)x(\d+)""").find(output) ?: return 1080 to 1920
        return match.groupValues[1].toInt() to match.groupValues[2].toInt()
    }

    fun push(local: File, remote: String): Boolean = exec("push", local.absolutePath, remote).ok

    fun pull(remote: String, local: File): Boolean {
        local.parentFile?.mkdirs()
        return exec("pull", remote, local.absolutePath).ok
    }

    /** Starts a command and returns immediately - used for screenrecord, which never exits on its own. */
    fun shellAsync(command: String): Process {
        Log.info("adb (async): $command")
        val invocation = buildList {
            add("adb")
            udid?.takeIf { it.isNotBlank() }?.let { addAll(listOf("-s", it)) }
            addAll(listOf("shell", command))
        }
        return ProcessBuilder(invocation).redirectErrorStream(true).start()
    }

    private fun runCommand(command: List<String>, timeoutSeconds: Long = 120): CommandResult {
        Log.info("adb: ${command.joinToString(" ")}")
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val output = StringBuilder()
        // Drain the pipe on its own thread: a blocking read would hang forever on long-lived commands.
        val drain = Thread {
            runCatching {
                process.inputStream.bufferedReader().forEachLine { line ->
                    synchronized(output) { output.appendLine(line) }
                }
            }
        }.apply {
            isDaemon = true
            start()
        }

        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!finished) process.destroyForcibly()
        drain.join(2000)
        val text = synchronized(output) { output.toString() }
        return if (finished) {
            CommandResult(command.joinToString(" "), process.exitValue(), text)
        } else {
            CommandResult(command.joinToString(" "), -1, "timed out after ${timeoutSeconds}s\n$text")
        }
    }
}
