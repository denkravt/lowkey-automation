package com.justan.shared.media

import com.justan.shared.logging.Log
import java.io.File
import java.util.concurrent.TimeUnit

data class VideoInfo(
    val width: Int,
    val height: Int,
    val durationMs: Long,
    val rotationDegrees: Int,
) {
    /** Width/height after the rotation metadata is applied - what the player will show. */
    val displayWidth: Int get() = if (rotationDegrees == 90 || rotationDegrees == 270) height else width
    val displayHeight: Int get() = if (rotationDegrees == 90 || rotationDegrees == 270) width else height

    val aspectRatio: Double get() = displayWidth.toDouble() / displayHeight.toDouble()
    val isLandscape: Boolean get() = displayWidth > displayHeight

    fun isCloseTo(target: Double, tolerance: Double = 0.05): Boolean = kotlin.math.abs(aspectRatio - target) <= tolerance
}

/** ffprobe wrapper: the only way to prove a recording really is 16:9, unrotated and the right length. */
object MediaProbe {

    fun probe(file: File): VideoInfo {
        check(file.exists() && file.length() > 0) { "no media to probe at ${file.absolutePath}" }
        val output = run(
            "ffprobe", "-v", "error",
            "-show_entries", "stream=width,height:stream_side_data=rotation",
            "-show_entries", "format=duration",
            "-of", "default=noprint_wrappers=1",
            file.absolutePath,
        )
        val width = output.lineValue("width")?.toIntOrNull()
        val height = output.lineValue("height")?.toIntOrNull()
        val duration = output.lineValue("duration")?.toDoubleOrNull() ?: 0.0
        val rotation = output.lineValue("rotation")?.toIntOrNull() ?: 0

        check(width != null && height != null) { "ffprobe found no video stream in ${file.name}:\n$output" }
        return VideoInfo(
            width = width,
            height = height,
            durationMs = (duration * 1000).toLong(),
            rotationDegrees = ((rotation % 360) + 360) % 360,
        )
    }

    private fun run(vararg command: String): String {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        check(process.waitFor(60, TimeUnit.SECONDS)) { "ffprobe timed out" }
        check(process.exitValue() == 0) { "ffprobe failed (${process.exitValue()}):\n$output" }
        return output
    }

    private fun String.lineValue(key: String): String? =
        lineSequence().firstOrNull { it.startsWith("$key=") }?.substringAfter('=')?.trim()

    /** True when ffprobe exists on this machine - tests fail with a clear message instead of a stack trace. */
    fun isAvailable(): Boolean = runCatching {
        val process = ProcessBuilder("ffprobe", "-version").redirectErrorStream(true).start()
        process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0
    }.getOrDefault(false).also { if (!it) Log.warn("ffprobe is not on PATH - media assertions will fail") }
}
