package com.justan.lowkey

import com.justan.lowkey.android.AndroidConfig
import com.justan.lowkey.ios.IosConfig
import com.justan.shared.android.AdbUtils
import com.justan.shared.android.AndroidDeviceSettingsManager
import com.justan.shared.android.DeviceUtils
import com.justan.shared.android.PermissionUtils
import com.justan.shared.appium.AppiumConfig
import com.justan.shared.appium.AppiumManager
import com.justan.shared.appium.DriverVersionCheck
import com.justan.shared.artifacts.TestArtifactManager
import com.justan.shared.ios.IosDeviceUtils
import com.justan.shared.logging.Log
import com.justan.shared.waitstrategy.RetryPolicy
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.ios.options.XCUITestOptions
import java.io.File
import java.net.URL
import java.time.Duration

/**
 * Creates the LowKey session per platform. Session creation is retried, and an APK that Appium
 * refuses to install (INSTALL_FAILED_TEST_ONLY) is installed through adb and the session retried.
 */
class LowKeyDriverManager(
    private val appiumConfig: AppiumConfig = AppiumConfig.load(),
    private val artifactManager: TestArtifactManager = TestArtifactManager(),
) {

    fun startAndroid(config: AndroidConfig = AndroidConfig.load()): AndroidDriver {
        DriverVersionCheck.requireMinimum("uiautomator2")
        val udid = DeviceUtils.resolveUdid(config.udid, config.requirePhysicalDevice)
        val adb = AdbUtils(udid)
        AndroidDeviceSettingsManager(adb).prepareForAutomation()

        val apk = artifactManager.resolve(config.apkPath)
        androidLeftovers(adb, config.appPackage)

        val options = UiAutomator2Options().apply {
            setUdid(udid)
            setApp(apk.absolutePath)
            setAppPackage(config.appPackage)
            setAppActivity(config.appActivity)
            setAppWaitActivity(config.launchActivity)
            setEnforceAppInstall(true)
            setFullReset(config.fullReset)
            setAutoGrantPermissions(config.autoGrantPermissions)
            setNewCommandTimeout(Duration.ofSeconds(config.newCommandTimeoutSeconds.toLong()))
            setCapability("appium:allowTestPackages", true)
            setCapability("appium:disableWindowAnimation", true)
            setCapability("appium:skipLogcatCapture", false)
        }

        val driver = RetryPolicy(attempts = 3, delayMs = 3000).execute("create Android session") {
            AndroidDriver(URL(appiumConfig.serverUrl), options)
        }
        PermissionUtils.grantCameraFlow(adb, config.appPackage)
        AppiumManager.setDriver(driver)
        Log.info("Android session up on $udid (${config.appPackage})")
        return driver
    }

    /** Fallback used when the driver reports an install failure on the first attempt. */
    fun installThroughAdb(udid: String, apk: File): Boolean = AdbUtils(udid).install(apk)

    fun startIos(config: IosConfig = IosConfig.load()): IOSDriver {
        DriverVersionCheck.requireMinimum("xcuitest")
        val udid = IosDeviceUtils.resolveUdid(config.udid)

        val options = XCUITestOptions().apply {
            setUdid(udid)
            setBundleId(config.bundleId)
            if (config.appPath.isNotBlank()) setApp(config.appPath)
            setEnforceAppInstall(true)
            setFullReset(true)
            setCapability("appium:usePrebuiltWDA", config.usePrebuiltWda)
            setDerivedDataPath(config.wdaBootstrapPath)
            // The base class launches the app so the first screen assertion sees a cold start.
            setCapability("appium:autoLaunch", config.autoLaunch)
            if (config.enableXcodeDebug) {
                setShowXcodeLog(true)
                setCapability("appium:xcodeOrgId", config.team)
                setCapability("appium:xcodeSigningId", "iPhone Developer")
            }
            setWdaLaunchTimeout(Duration.ofMinutes(4))
            setWdaConnectionTimeout(Duration.ofMinutes(4))
            setNewCommandTimeout(Duration.ofMinutes(3))
        }

        val driver = RetryPolicy(attempts = 3, delayMs = 5000).execute("create iOS session") {
            IOSDriver(URL(appiumConfig.serverUrl), options)
        }
        AppiumManager.setDriver(driver)
        Log.info("iOS session up on $udid (${config.bundleId})")
        return driver
    }

    private fun androidLeftovers(adb: AdbUtils, packageName: String) {
        if (adb.isInstalled(packageName)) {
            Log.info("Removing previous install of $packageName")
            adb.uninstall(packageName)
        }
    }
}
