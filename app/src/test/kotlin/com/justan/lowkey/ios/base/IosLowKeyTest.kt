package com.justan.lowkey.ios.base

import com.justan.extensions.BeforeAllFailureCaptureExtension
import com.justan.extensions.FailureArtifacts
import com.justan.extensions.TestArtifactsExtension
import com.justan.lowkey.LowKeyDriverManager
import com.justan.lowkey.ios.IosConfig
import com.justan.shared.appium.AppiumManager
import com.justan.shared.ios.IosLogCapture
import com.justan.shared.logging.Log
import io.appium.java_client.ios.IOSDriver
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith

/**
 * iOS lifecycle. Scaffolded only: it needs a device, a signed WDA and the xcuitest driver, none of
 * which are available on this machine yet.
 */
@Tag("LOWKEY")
@Tag("IOS")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(TestArtifactsExtension::class, BeforeAllFailureCaptureExtension::class)
abstract class IosLowKeyTest {

    protected val iosConfig: IosConfig = IosConfig.load()
    protected lateinit var driver: IOSDriver
    private var logCapture: IosLogCapture? = null

    @BeforeAll
    fun setUpSuite() {
        Log.section("ios suite setup: ${javaClass.simpleName}")
        AppiumManager.start()
        driver = LowKeyDriverManager().startIos(iosConfig)
        FailureArtifacts.registerDriverSupplier { if (AppiumManager.hasDriver()) AppiumManager.driver() else null }
    }

    @BeforeEach
    fun startCapture(info: TestInfo) {
        logCapture = IosLogCapture(iosConfig.udid).also { it.start("${javaClass.simpleName}-${info.displayName}") }
    }

    @AfterEach
    fun stopCapture(info: TestInfo) {
        logCapture?.stop(keep = false)?.let { Log.warn("ios log kept: $it") }
        logCapture = null
        Log.info("finished ${info.displayName}")
    }

    @AfterAll
    fun tearDownSuite() {
        AppiumManager.stop()
    }
}
