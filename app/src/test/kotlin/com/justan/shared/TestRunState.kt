package com.justan.shared

import java.util.concurrent.ConcurrentHashMap

/**
 * Per-test failure state. JUnit gives @AfterEach no result, so the extension drops a marker here and
 * the base class uses it to decide whether artifacts are kept or discarded.
 */
object TestRunState {
    private val failed = ConcurrentHashMap.newKeySet<String>()
    private val started = ConcurrentHashMap<String, Long>()

    fun markStarted(uniqueId: String) {
        started[uniqueId] = System.currentTimeMillis()
    }

    fun markFailed(uniqueId: String) {
        failed.add(uniqueId)
    }

    fun isFailed(uniqueId: String): Boolean = failed.contains(uniqueId)

    fun durationMs(uniqueId: String): Long = started[uniqueId]?.let { System.currentTimeMillis() - it } ?: 0L

    fun clear(uniqueId: String) {
        failed.remove(uniqueId)
        started.remove(uniqueId)
    }
}
