package com.justan.extensions

import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan

/** Suite-level aggregates written to build/test-results/suite-summary.txt. */
class SuiteSummaryListener : TestExecutionListener {
    private val started = AtomicInteger()
    private val passed = AtomicInteger()
    private val failed = AtomicInteger()
    private val skipped = AtomicInteger()
    private var startedAt: Instant = Instant.now()
    private val retried = mutableSetOf<String>()

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        startedAt = Instant.now()
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        if (testIdentifier.isTest) started.incrementAndGet()
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        if (!testIdentifier.isTest) return
        when (testExecutionResult.status) {
            TestExecutionResult.Status.SUCCESSFUL -> passed.incrementAndGet()
            TestExecutionResult.Status.FAILED -> failed.incrementAndGet()
            TestExecutionResult.Status.ABORTED -> skipped.incrementAndGet()
        }
    }

    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String) {
        if (testIdentifier.isTest) skipped.incrementAndGet()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        val seconds = Duration.between(startedAt, Instant.now()).seconds
        val summary = buildString {
            appendLine("suite summary")
            appendLine("started: ${started.get()}")
            appendLine("passed: ${passed.get()}")
            appendLine("failed: ${failed.get()}")
            appendLine("skipped: ${skipped.get()}")
            appendLine("durationSeconds: $seconds")
            if (retried.isNotEmpty()) appendLine("retried: ${retried.joinToString()}")
        }
        File("build/test-results").mkdirs()
        File("build/test-results/suite-summary.txt").writeText(summary)
        println(summary)
    }
}
