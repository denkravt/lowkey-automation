package com.justan.lowkey.android.support

import com.justan.shared.android.AdbUtils
import com.justan.shared.logging.Log
import java.io.File

/** Reads what the app actually wrote: private clip files and the gallery export. */
object DeviceMedia {

    private const val PACKAGE = "com.justan.lowkeycam"
    private const val CLIPS_DIR = "files/clips"

    /** Newest recording in the app sandbox, pulled to [target]. */
    fun newestClip(adb: AdbUtils, target: File): File? {
        val name = adb.newestAppFile(PACKAGE, CLIPS_DIR) ?: run {
            Log.warn("no clips in $CLIPS_DIR")
            return null
        }
        val pulled = adb.pullAppFile(PACKAGE, "$CLIPS_DIR/$name", target)
        Log.info("pulled clip $name -> ${target.absolutePath} (ok=$pulled)")
        return target.takeIf { pulled }
    }

    /** Newest video in Movies/LowKey, pulled to [target]. */
    fun newestGalleryExport(adb: AdbUtils, target: File): File? {
        val output = adb.shell(
            "content query --uri content://media/external/video/media " +
                "--projection _data:relative_path:date_added --sort \"date_added DESC\"",
        ).output
        val path = output.lineSequence()
            .filter { it.contains("Movies/LowKey") }
            .mapNotNull { Regex("""_data=([^,]+)""").find(it)?.groupValues?.get(1)?.trim() }
            .firstOrNull()
        if (path == null) {
            Log.warn("no Movies/LowKey row in MediaStore:\n${output.take(400)}")
            return null
        }
        return target.takeIf { adb.pull(path, target) }
    }
}
