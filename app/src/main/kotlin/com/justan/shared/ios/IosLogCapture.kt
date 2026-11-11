package com.justan.shared.ios

import com.justan.shared.logging.Log
import java.io.File

/** Streams idevicesyslog to a file while a test runs. */
class IosLogCapture(
    private val udid: String,
    private val outputDir: File = File("build/artifacts/logs"),
) {
    private var process: Process? = null
    private var target: File? = null

    fun start(testName: String) {
        outputDir.mkdirs()
        val file = File(outputDir, "${testName.replace(Regex("[^A-Za-z0-9._-]"), "_")}-${System.currentTimeMillis()}.log")
        process = ProcessBuilder("idevicesyslog", "-u", udid)
            .redirectErrorStream(true)
            .redirectOutput(file)
            .start()
        target = file
        Log.info("ios syslog capture started: $file")
    }

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
}
