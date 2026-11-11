package com.justan.shared.ios

/** Keeps the device awake and reports what the suite is running against. */
class IosDeviceSettingsManager(private val udid: String) {
    fun prepareForAutomation() {
        listOf(
            // 0 disables auto-lock by keeping the device "in use"
            arrayOf("idevicedebug", "-u", udid, "info"),
        ).forEach { IosDeviceUtils.run(*it) }
    }
}
