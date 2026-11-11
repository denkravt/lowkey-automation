package com.justan.shared.android

import com.justan.shared.config.Paths
import com.justan.shared.logging.Log
import java.io.File

/** Streams `adb logcat` to a file for the duration of one test. */
class AndroidLogCapture(
    private val adb: AdbUtils,
    private val outputDir: File = Paths.resolve("build/artifacts/logs").toFile(),
) {
    private var process: Process? = null
    private var target: File? = null

    fun start(testName: String) {
        outputDir.mkdirs()
        val file = File(outputDir, "${testName.sanitised()}-${System.currentTimeMillis()}.log")
        val command = buildList {
            add("adb")
            adb.udid?.takeIf { it.isNotBlank() }?.let { addAll(listOf("-s", it)) }
            addAll(listOf("logcat", "-v", "time"))
        }
        process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .redirectOutput(file)
            .start()
        target = file
        Log.info("logcat capture started: $file")
    }

    /** Keeps the file when the test failed, deletes it otherwise. */
    fun stop(keep: Boolean): File? {
        process?.let {
            it.destroy()
            runCatching { it.waitFor() }
        }
        process = null
        val file = target
        if (file != null && !keep) file.delete()
        target = null
        return if (keep) file else null
    }

    private fun String.sanitised(): String = replace(Regex("[^A-Za-z0-9._-]"), "_")
}
