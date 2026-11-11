package com.justan.shared.waitstrategy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException

/** No device needed: the clock is injected so the tests run instantly. */
@Tag("PR-CHECK")
class PollingWaitStrategyTest {

    private var now = 0L
    private val sleeps = mutableListOf<Long>()
    private val strategy = PollingWaitStrategy(
        pollIntervalMs = 500L,
        sleeper = { sleeps += it; now += it },
        clock = { now },
    )

    @Test
    fun `returns the first non-null value`() {
        val value = strategy.poll("element", 5000L) { "ready" }
        assertEquals("ready", value)
    }

    @Test
    fun `throws with the description when the timeout expires`() {
        val error = assertThrows(WaitTimeoutException::class.java) {
            strategy.poll("missing element", 1500L) { null }
        }
        assertTrue(error.message!!.contains("missing element"))
        assertTrue(error.message!!.contains("1500ms"))
        assertEquals(3, sleeps.size)
    }

    @Test
    fun `keeps polling while the element is stale or absent`() {
        var attempts = 0
        val value = strategy.poll("eventually there", 5000L) {
            attempts++
            when (attempts) {
                1 -> throw NoSuchElementException("not yet")
                2 -> throw StaleElementReferenceException("stale")
                3 -> null
                else -> "found"
            }
        }
        assertEquals("found", value)
        assertEquals(4, attempts)
    }

    @Test
    fun `waitUntil reports false instead of throwing`() {
        val result = strategy.waitUntil("never true", 1000L) { false }
        assertTrue(!result)
    }
}
