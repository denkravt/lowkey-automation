package com.justan.shared.android

import com.justan.shared.logging.Log

/**
 * Resolves which device a run targets, honouring the explicit udid from config first and
 * refusing to fall back to an emulator unless the config allows it.
 */
object DeviceUtils {
    fun resolveUdid(configuredUdid: String, requirePhysical: Boolean): String {
        val adb = AdbUtils()
        if (configuredUdid.isNotBlank()) {
            val match = adb.listDevices().firstOrNull { it.udid == configuredUdid }
            check(match != null) { "Configured DEVICE_UDID=$configuredUdid is not connected (adb devices -l)" }
            Log.info("Using configured device ${match.udid} (${match.model ?: "unknown model"})")
            return match.udid
        }

        val online = adb.listDevices()
        check(online.isNotEmpty()) { "No adb devices connected" }

        val physical = online.filterNot { it.isEmulator }
        val chosen = when {
            physical.isNotEmpty() -> physical.first()
            requirePhysical -> error(
                "REQUIRE_PHYSICAL_DEVICE is on but only emulators are attached: " +
                    online.joinToString { it.udid },
            )
            else -> online.first()
        }
        Log.info("Resolved device ${chosen.udid} (${chosen.model ?: "unknown"}, emulator=${chosen.isEmulator})")
        return chosen.udid
    }
}
