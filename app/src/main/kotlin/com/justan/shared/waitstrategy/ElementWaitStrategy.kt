package com.justan.shared.waitstrategy

/** Timeout surface a screen can plug in - PollingWaitStrategy is the production implementation. */
interface ElementWaitStrategy {
    fun <T : Any> poll(description: String, timeoutMs: Long, supplier: () -> T?): T?
}

class WaitTimeoutException(
    val description: String,
    val timeoutMs: Long,
    val lastNameError: String?,
) : RuntimeException("Timed out after ${timeoutMs}ms waiting for $description" + (lastNameError?.let { " (last error: $it)" } ?: ""))
