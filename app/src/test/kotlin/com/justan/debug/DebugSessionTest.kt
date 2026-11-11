package com.justan.debug

import com.justan.lowkey.LowKeyDriverManager
import com.justan.shared.android.AdbUtils
import com.justan.shared.appium.AppiumManager
import com.justan.shared.logging.Log
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/** Throwaway harness check: opens one session, proves the app is foregrounded, closes it. */
@Tag("DEBUG")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DebugSessionTest {

    private val manager = LowKeyDriverManager()

    @BeforeAll
    fun startServer() {
        AppiumManager.start()
    }

    @AfterAll
    fun stopServer() {
        AppiumManager.stop()
    }

    @Test
    fun `session opens and LowKey is in the foreground`() {
        val driver = manager.startAndroid()
        val udid = driver.capabilities.getCapability("udid").toString()
        Log.info("session id ${driver.sessionId} on $udid")

        val foreground = AdbUtils(udid).shell("dumpsys activity activities")
        driver.quit()
        AppiumManager.quitDriver()

        assertTrue(
            foreground.output.contains("com.justan.lowkeycam"),
            "LowKey should be the foreground package after the session started",
        )
    }
}
