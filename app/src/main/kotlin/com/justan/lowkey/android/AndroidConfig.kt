package com.justan.lowkey.android

import com.justan.shared.config.Paths
import com.typesafe.config.ConfigFactory

data class AndroidConfig(
    val appPackage: String,
    val appActivity: String,
    val apkPath: String,
    val remotePath: String,
    val udid: String,
    val requirePhysicalDevice: Boolean,
    val fullReset: Boolean,
    val autoGrantPermissions: Boolean,
    val newCommandTimeoutSeconds: Int,
    val enableScreenRecording: Boolean,
    val screenRecordingBitRate: String,
    val screenRecordingTimeLimitSeconds: Int,
) {
    val launchActivity: String
        get() = if (appActivity.startsWith(".")) appPackage + appActivity else appActivity

    companion object {
        fun load(): AndroidConfig {
            val config = ConfigFactory.load("android/android.conf").getConfig("android")
            val run = config.getConfig("run")
            return AndroidConfig(
                appPackage = config.getString("appPackage"),
                appActivity = config.getString("appActivity"),
                apkPath = Paths.resolveString(config.getConfig("app").getString("apkPath")),
                remotePath = config.getConfig("app").getString("remotePath"),
                udid = config.getConfig("device").getString("udid"),
                requirePhysicalDevice = config.getConfig("device").getBoolean("requirePhysicalDevice"),
                fullReset = run.getBoolean("fullReset"),
                autoGrantPermissions = run.getBoolean("autoGrantPermissions"),
                newCommandTimeoutSeconds = run.getInt("newCommandTimeoutSeconds"),
                enableScreenRecording = run.getBoolean("enableScreenRecording"),
                screenRecordingBitRate = run.getString("screenRecordingBitRate"),
                screenRecordingTimeLimitSeconds = run.getInt("screenRecordingTimeLimitSeconds"),
            )
        }
    }
}
