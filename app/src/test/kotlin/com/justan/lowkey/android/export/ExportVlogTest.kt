package com.justan.lowkey.android.export

import com.justan.lowkey.android.base.AndroidLowKeyTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: the export stitches the clips and publishes the result to the gallery. */
@Tag("EXPORT")
class ExportVlogTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("export vlog reports a finished export saved to the gallery")
    fun exportVlog() {
        val name = uniqueProjectName("Export")
        val project = createProject(name)
        try {
            val withClip = recordClip(project, seconds = 2)
            val note = withClip.exportVlog().awaitExportNote(240_000L)
            assertTrue(note.startsWith("Exported"), "export should report success, got '$note'")
            assertTrue(
                note.contains("saved to gallery"),
                "the export should land in the gallery (Movies/LowKey), got '$note'",
            )
        } finally {
            cleanupProject(name)
        }
    }
}
