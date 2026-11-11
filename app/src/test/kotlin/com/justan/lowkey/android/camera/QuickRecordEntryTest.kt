package com.justan.lowkey.android.camera

import com.justan.lowkey.android.base.AndroidLowKeyTest
import com.justan.lowkey.android.screens.HomeScreen
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** T0: the home card's two quick-record entry points open the camera in the right mode. */
@Tag("CAMERA")
class QuickRecordEntryTest : AndroidLowKeyTest() {

    @Test
    @Tag("T0")
    @DisplayName("low-key and normal buttons open the matching camera mode")
    fun quickRecordEntries() {
        val home = HomeScreen(driver)
        assertTrue(home.isDisplayed(), "home should be visible")

        val lowKeyCamera = home.quickRecord(lowKey = true)
        assertTrue(lowKeyCamera.isDisplayed(), "the Low-key button opens the camera")
        assertTrue(
            lowKeyCamera.modeLabel().contains("LOW-KEY"),
            "expected low-key mode, got '${lowKeyCamera.modeLabel()}'",
        )
        lowKeyCamera.closeToHome()

        val normalCamera = HomeScreen(driver).quickRecord(lowKey = false)
        assertTrue(normalCamera.isDisplayed(), "the Normal button opens the camera")
        assertTrue(
            normalCamera.modeLabel().contains("STANDARD"),
            "expected standard mode, got '${normalCamera.modeLabel()}'",
        )
        normalCamera.closeToHome()
    }
}
