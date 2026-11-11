package com.justan.shared.waitstrategy

import com.justan.shared.logging.Log

/** Bounded retries for flaky driver interactions (session creation, install, clicks). */
data class RetryPolicy(
    val attempts: Int = 3,
    val delayMs: Long = 1000L,
    val backoffMultiplier: Double = 2.0,
) {
    init {
        require(attempts >= 1) { "attempts must be >= 1" }
    }

    fun <T> execute(description: String, action: () -> T): T {
        var delay = delayMs
        var lastError: Throwable? = null
        repeat(attempts) { index ->
            try {
                return action()
            } catch (error: Throwable) {
                lastError = error
                Log.warn("$description failed (attempt ${index + 1}/$attempts): ${error.message?.lineSequence()?.firstOrNull()}")
                if (index < attempts - 1) Thread.sleep(delay).also { delay = (delay * backoffMultiplier).toLong() }
            }
        }
        throw lastError ?: error("$description failed without an error")
    }

    companion object {
        val DEFAULT = RetryPolicy()
    }
}
