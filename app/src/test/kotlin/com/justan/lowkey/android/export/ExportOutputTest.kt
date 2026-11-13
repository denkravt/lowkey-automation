package com.justan.lowkey.android.export

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.support.DeviceMedia
import com.justan.shared.media.MediaProbe
import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: the export is not just announced - the file in the gallery is really one 16:9 video. */
@Tag("EXPORT")
class ExportOutputTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("the exported gallery file exists, is 16:9 and lasts as long as the clips")
    fun exportedFileIsReal() {
        assumeTrue(MediaProbe.isAvailable(), "ffprobe is required for output assertions")

        val name = uniqueProjectName("ExportFile")
        val project = createProject(name)
        try {
            val withClip = recordClip(project, seconds = 3)
            val clipSeconds = withClip.summarySeconds()

            val note = withClip.exportVlog().awaitExportNote(240_000L)
            assertTrue(note.startsWith("Exported"), "export should succeed, got '$note'")

            val target = File("build/artifacts/media/${name.replace(' ', '_')}-export.mp4")
            val exported = DeviceMedia.newestGalleryExport(adb, target)
            assertNotNull(exported, "the export should be present in Movies/LowKey")

            val info = MediaProbe.probe(exported!!)
            assertTrue(info.isLandscape, "exported video should be landscape, got ${info.width}x${info.height}")
            assertTrue(info.isCloseTo(16.0 / 9.0), "exported video should be 16:9, got ${info.aspectRatio}")
            assertTrue(
                info.durationMs in (clipSeconds * 1000L - 1500L)..(clipSeconds * 1000L + 4000L),
                "exported duration ${info.durationMs}ms should match the ${clipSeconds}s of clips",
            )
        } finally {
            cleanupProject(name)
        }
    }
}
