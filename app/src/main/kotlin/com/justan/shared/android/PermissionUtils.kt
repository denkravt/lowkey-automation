package com.justan.shared.android

/** Runtime permissions the camera flow needs; granting them up front keeps tests off the dialog path. */
object PermissionUtils {
    private val CAMERA_FLOW = listOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_MEDIA_VIDEO",
    )

    fun grantCameraFlow(adb: AdbUtils, packageName: String) {
        CAMERA_FLOW.forEach { adb.grantPermission(packageName, it) }
    }
}
