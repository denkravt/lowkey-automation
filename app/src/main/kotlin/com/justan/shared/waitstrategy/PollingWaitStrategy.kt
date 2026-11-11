package com.justan.shared.waitstrategy

import com.justan.shared.logging.Log
import java.util.concurrent.atomic.AtomicReference
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriverException

/**
 * Polls a supplier until it returns non-null or the deadline passes. NoSuchElement and stale
 * element errors are expected while the UI is settling, so they only count as "keep waiting".
 */
class PollingWaitStrategy(
    private val pollIntervalMs: Long = 500L,
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) },
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ElementWaitStrategy {

    private val tolerated = setOf(NoSuchElementException::class.java, StaleElementReferenceException::class.java)

    override fun <T : Any> poll(description: String, timeoutMs: Long, supplier: () -> T?): T? {
        val deadline = clock() + timeoutMs
        val lastError = AtomicReference<String?>(null)
        var attempts = 0

        while (clock() <= deadline) {
            attempts++
            val value = attempt(supplier, lastError)
            if (value != null) return value
            if (clock() + pollIntervalMs > deadline) break
            sleeper(pollIntervalMs)
        }

        Log.warn("wait failed [$description] after ${attempts} polls: ${lastError.get() ?: "not found"}")
        throw WaitTimeoutException(description, timeoutMs, lastError.get())
    }

    /** Convenience: true when the condition held within the timeout. */
    fun waitUntil(description: String, timeoutMs: Long, condition: () -> Boolean): Boolean =
        runCatching { poll(description, timeoutMs) { if (condition()) Unit else null } }.isSuccess

    /** Returns true as soon as the supplier stops finding an element. */
    fun waitUntilGone(description: String, timeoutMs: Long, supplier: () -> Any?): Boolean =
        waitUntil(description, timeoutMs) { runCatching { supplier() }.getOrNull() == null }

    private fun <T : Any> attempt(supplier: () -> T?, lastError: AtomicReference<String?>): T? = try {
        supplier()?.also { lastError.set(null) }
    } catch (error: WebDriverException) {
        lastError.set("${error.javaClass.simpleName}: ${error.message?.lineSequence()?.firstOrNull()}")
        if (tolerated.none { it.isInstance(error) }) {
            Log.warn("non-transient error while waiting: ${error.message?.lineSequence()?.firstOrNull()}")
        }
        null
    }
}
