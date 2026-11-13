package com.justan.lowkey.android.recording

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.support.DeviceMedia
import com.justan.shared.media.MediaProbe
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: a low-key take is a real 16:9 landscape file with no rotation metadata. */
@Tag("RECORDING")
class ClipOutputTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("the low-key clip on disk is 16:9, landscape and carries no rotation")
    fun clipFileIsLandscape() {
        assumeTrue(MediaProbe.isAvailable(), "ffprobe is required for output assertions")

        val name = uniqueProjectName("ClipFile")
        val project = createProject(name)
        try {
            recordClip(project, seconds = 3)

            val target = File("build/artifacts/media/${name.replace(' ', '_')}-clip.mp4")
            val clip = DeviceMedia.newestClip(adb, target)
            assertTrue(clip != null, "a clip file should exist in the app sandbox")

            val info = MediaProbe.probe(clip!!)
            assertTrue(info.isLandscape, "clip should be landscape, got ${info.width}x${info.height}")
            assertTrue(
                info.isCloseTo(16.0 / 9.0),
                "clip should be cropped to 16:9, got ${info.width}x${info.height} (${"%.3f".format(info.aspectRatio)})",
            )
            assertEquals(0, info.rotationDegrees, "clip must not carry rotation metadata")
            assertTrue(info.durationMs in 2_000L..6_000L, "clip should be ~3s, was ${info.durationMs}ms")
        } finally {
            cleanupProject(name)
        }
    }
}
