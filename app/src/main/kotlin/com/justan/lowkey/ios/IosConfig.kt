package com.justan.lowkey.ios

import com.justan.shared.config.Paths
import com.typesafe.config.ConfigFactory

data class IosConfig(
    val bundleId: String,
    val appPath: String,
    val wdaBootstrapPath: String,
    val team: String,
    val udid: String,
    val usePrebuiltWda: Boolean,
    val autoLaunch: Boolean,
    val enableXcodeDebug: Boolean,
) {
    companion object {
        fun load(): IosConfig {
            val config = ConfigFactory.load("ios/ios.conf").getConfig("ios")
            val app = config.getConfig("app")
            val run = config.getConfig("run")
            return IosConfig(
                bundleId = config.getString("bundleId"),
                appPath = app.getString("appPath").takeIf { it.isNotBlank() }?.let { Paths.resolveString(it) } ?: "",
                wdaBootstrapPath = Paths.resolveString(app.getString("wdaBootstrapPath")),
                team = app.getString("team"),
                udid = config.getConfig("device").getString("udid"),
                usePrebuiltWda = run.getBoolean("usePrebuiltWDA"),
                autoLaunch = run.getBoolean("autoLaunch"),
                enableXcodeDebug = run.getBoolean("enableXcodeDebug"),
            )
        }
    }
}
