package com.justan.shared.android

import com.justan.shared.config.Paths
import com.justan.shared.logging.Log
import java.io.File

/** Device-side screenrecord, pulled to the host when the test ends. */
class AndroidScreenRecorder(
    private val adb: AdbUtils,
    private val outputDir: File = Paths.resolve("build/artifacts/recordings").toFile(),
    private val bitRate: String = "4M",
    private val timeLimitSeconds: Int = 180,
) {
    private var remotePath: String? = null
    private var localTarget: File? = null

    fun start(testName: String) {
        outputDir.mkdirs()
        val remote = "/sdcard/${testName.sanitised()}-${System.currentTimeMillis()}.mp4"
        adb.shellAsync("screenrecord --bit-rate $bitRate --time-limit $timeLimitSeconds $remote")
        remotePath = remote
        localTarget = File(outputDir, remote.substringAfterLast('/'))
        // screenrecord blocks the shell; give it a moment to spin up before the test drives the UI.
        Thread.sleep(1200)
        Log.info("screen recording started: $remote")
    }

    /** Stops the recording and returns the pulled file when [keep] is true. */
    fun stop(keep: Boolean): File? {
        val remote = remotePath ?: return null
        adb.shell("pkill -INT screenrecord")
        Thread.sleep(1200)
        val local = localTarget
        val pulled = if (keep && local != null) adb.pull(remote, local) else false
        adb.shell("rm -f $remote")
        remotePath = null
        localTarget = null
        return if (pulled) local else null
    }

    private fun String.sanitised(): String = replace(Regex("[^A-Za-z0-9._-]"), "_")
}
