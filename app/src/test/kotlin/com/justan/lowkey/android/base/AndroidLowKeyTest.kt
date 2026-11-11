package com.justan.lowkey.android.base

import com.justan.extensions.BeforeAllFailureCaptureExtension
import com.justan.extensions.FailureArtifacts
import com.justan.extensions.TestArtifactsExtension
import com.justan.lowkey.LowKeyDriverManager
import com.justan.lowkey.android.AndroidConfig
import com.justan.shared.TestRunState
import com.justan.shared.android.AdbUtils
import com.justan.shared.android.AndroidLogCapture
import com.justan.shared.android.AndroidScreenRecorder
import com.justan.shared.appium.AppiumManager
import com.justan.lowkey.android.screens.HomeScreen
import com.justan.lowkey.android.screens.ProjectScreen
import com.justan.shared.logging.Log
import com.justan.shared.testdata.TestDataManager
import io.appium.java_client.android.AndroidDriver
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Android lifecycle for every LowKey suite: server, artifact, session, then per-test log and
 * screen capture. Artifacts survive only when the test fails.
 */
@Tag("LOWKEY")
@Tag("ANDROID")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(TestArtifactsExtension::class, BeforeAllFailureCaptureExtension::class)
abstract class AndroidLowKeyTest {

    protected val androidConfig: AndroidConfig = AndroidConfig.load()
    protected lateinit var driver: AndroidDriver
    protected lateinit var adb: AdbUtils
    protected var deviceUdid: String = ""

    private var logCapture: AndroidLogCapture? = null
    private var screenRecorder: AndroidScreenRecorder? = null

    @BeforeAll
    fun setUpSuite() {
        Log.section("suite setup: ${javaClass.simpleName}")
        AppiumManager.start()
        driver = LowKeyDriverManager().startAndroid(androidConfig)
        deviceUdid = driver.capabilities.getCapability("udid")?.toString().orEmpty()
        adb = AdbUtils(deviceUdid)
        FailureArtifacts.registerDriverSupplier { if (AppiumManager.hasDriver()) AppiumManager.driver() else null }
    }

    @BeforeEach
    fun startCapture(info: TestInfo) {
        // Each test owns its state: whatever the previous one left open, we start on home.
        resetToHome()
        val name = "${javaClass.simpleName}-${info.displayName}"
        logCapture = AndroidLogCapture(adb).also { it.start(name) }
        if (androidConfig.enableScreenRecording) {
            screenRecorder = AndroidScreenRecorder(
                adb = adb,
                bitRate = androidConfig.screenRecordingBitRate,
                timeLimitSeconds = androidConfig.screenRecordingTimeLimitSeconds,
            ).also { it.start(name) }
        }
    }

    @AfterEach
    fun stopCapture(info: TestInfo) {
        val keep = TestRunState.isFailed(testKey(info))
        logCapture?.stop(keep)?.let { Log.warn("log kept: $it") }
        screenRecorder?.stop(keep)?.let { Log.warn("recording kept: $it") }
        logCapture = null
        screenRecorder = null
        if (keep) Log.warn("artifacts retained for failed test ${info.displayName}")
        TestRunState.clear(testKey(info))
    }

    /** Backs out of dialogs/screens until home is on screen. */
    protected fun resetToHome() {
        repeat(5) {
            if (HomeScreen(driver).isDisplayed(1_000L)) return
            runCatching { driver.navigate().back() }
        }
        check(HomeScreen(driver).isDisplayed(5_000L)) { "could not return to the home screen" }
    }

    /** Unique name per test so projects never collide between runs. */
    protected fun uniqueProjectName(prefix: String): String =
        "$prefix ${System.currentTimeMillis() % 1_000_000}"

    /** Creates a project through the UI and opens it. */
    protected fun createProject(name: String): ProjectScreen {
        val home = HomeScreen(driver)
        home.createProject(name)
        assertTrue(home.projectExists(name), "project '$name' should appear in the list after creating it")
        return home.openProject(name)
    }

    /** Records a take of [seconds] from the project screen and returns to the project. */
    protected fun recordClip(project: ProjectScreen, seconds: Int = 3): ProjectScreen {
        val before = project.clipCount()
        val camera = project.startRecording()
        assertTrue(camera.isDisplayed(), "camera should open from the project record tile")
        camera.startRecording().awaitRecordedSeconds(seconds).stopRecording()
        val back = camera.close()
        assertTrue(
            back.awaitClipCount(before + 1, TestDataManager().cropTimeoutMs()),
            "the take should land in the project (was $before, now ${back.clipCount()})",
        )
        return back
    }

    /** Deletes a project from its own screen, leaving the app on home. */
    protected fun deleteProject(project: ProjectScreen): HomeScreen = project.deleteProject()

    /**
     * State-independent cleanup: go home, find the project by name, delete it. Never masks the real
     * failure inside a finally block.
     */
    protected fun cleanupProject(name: String) {
        runCatching {
            resetToHome()
            val home = HomeScreen(driver)
            if (home.projectExists(name, 2_000L)) {
                home.openProject(name).deleteProject()
            }
        }.onFailure { Log.warn("cleanup of '$name' failed: ${it.message}") }
    }

    private fun testKey(info: TestInfo): String =
        "${javaClass.simpleName}#${info.testMethod.map { it.name }.orElse(info.displayName)}"

    @AfterAll
    fun tearDownSuite() {
        Log.section("suite teardown: ${javaClass.simpleName}")
        AppiumManager.stop()
    }
}
