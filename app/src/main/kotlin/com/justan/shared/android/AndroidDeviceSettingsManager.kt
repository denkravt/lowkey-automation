package com.justan.shared.android

import com.justan.shared.logging.Log

/** Puts the device into a predictable state before a suite runs. */
class AndroidDeviceSettingsManager(private val adb: AdbUtils) {

    fun prepareForAutomation() {
        listOf(
            "settings put global window_animation_scale 0",
            "settings put global transition_animation_scale 0",
            "settings put global animator_duration_scale 0",
            "settings put system screen_off_timeout 1800000",
            "svc power stayon true",
        ).forEach { command ->
            val result = adb.shell(command)
            if (!result.ok) Log.warn("could not apply '$command': ${result.output.trim()}")
        }
        Log.info("Device prepared for automation (animations off, stay awake)")
    }

    /** 0 = portrait, 1 = landscape (device rotated counter-clockwise), 2 = reverse portrait, 3 = reverse landscape. */
    fun setRotation(rotation: Int) {
        adb.shell("settings put system accelerometer_rotation 0")
        adb.shell("settings put system user_rotation $rotation")
        Log.info("Device rotation forced to $rotation")
    }

    fun restoreAutoRotate() {
        adb.shell("settings put system accelerometer_rotation 1")
    }
}
