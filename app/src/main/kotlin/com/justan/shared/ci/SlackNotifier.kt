package com.justan.shared.ci

import com.justan.shared.logging.Log
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Summarises JUnit XML into a Slack message. With no SLACK_BOT_TOKEN it prints the message and
 * exits - the dry-run path CI uses on forks.
 */
class SlackNotifier(
    private val webhookUrl: String? = System.getenv("SLACK_WEBHOOK_URL"),
    private val botToken: String? = System.getenv("SLACK_BOT_TOKEN"),
    private val channel: String? = System.getenv("SLACK_CHANNEL"),
    private val runUrl: String? = System.getenv("BUILD_URL"),
) {
    data class SuiteSummary(
        val name: String,
        val tests: Int,
        val failures: Int,
        val skipped: Int,
        val timeSeconds: Double,
        val retried: Int,
    ) {
        val passed: Int get() = tests - failures - skipped
    }

    fun collect(resultsDir: File): List<SuiteSummary> {
        if (!resultsDir.exists()) return emptyList()
        return resultsDir.walkTopDown()
            .filter { it.isFile && it.extension == "xml" && it.name.startsWith("TEST-") }
            .mapNotNull { parse(it) }
            .toList()
    }

    fun buildMessage(summaries: List<SuiteSummary>): String {
        if (summaries.isEmpty()) return "No JUnit results found - nothing to report."
        val tests = summaries.sumOf { it.tests }
        val failures = summaries.sumOf { it.failures }
        val skipped = summaries.sumOf { it.skipped }
        val passed = tests - failures - skipped
        val duration = summaries.sumOf { it.timeSeconds }
        val headline = if (failures == 0) "PASS" else "FAIL"

        return buildString {
            appendLine("$headline - LowKey automation")
            appendLine("$passed passed, $failures failed, $skipped skipped (${"%.1f".format(duration)}s)")
            if (failures > 0) {
                appendLine()
                summaries.filter { it.failures > 0 }.forEach { appendLine("- ${it.name}: ${it.failures} failed") }
            }
            runUrl?.let { appendLine(); appendLine("Run: $it") }
        }.trim()
    }

    /** Returns true when a message was actually delivered. */
    fun postFromResults(resultsDir: File): Boolean {
        val message = buildMessage(collect(resultsDir))
        if (webhookUrl.isNullOrBlank() && botToken.isNullOrBlank()) {
            Log.warn("Slack dry-run (no SLACK_WEBHOOK_URL / SLACK_BOT_TOKEN):")
            println(message)
            return false
        }
        Log.info("Slack post to ${channel ?: "default channel"} (${message.lines().size} lines)")
        return deliver(message)
    }

    private fun deliver(message: String): Boolean {
        val payload = """{"text":${quote(message)}}"""
        val connection = if (!webhookUrl.isNullOrBlank()) {
            java.net.URL(webhookUrl).openConnection()
        } else {
            java.net.URL("https://slack.com/api/chat.postMessage").openConnection()
        }
        return runCatching {
            val http = connection as java.net.HttpURLConnection
            http.requestMethod = "POST"
            http.doOutput = true
            http.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            if (!botToken.isNullOrBlank()) http.setRequestProperty("Authorization", "Bearer $botToken")
            http.outputStream.use { it.write(payload.toByteArray()) }
            val code = http.responseCode
            Log.info("Slack response: $code")
            code in 200..299
        }.getOrElse {
            Log.error("Slack post failed", it)
            false
        }
    }

    private fun parse(file: File): SuiteSummary? = runCatching {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val root = document.documentElement
        SuiteSummary(
            name = root.getAttribute("name").substringAfterLast('.'),
            tests = root.getAttribute("tests").toIntOrNull() ?: 0,
            failures = root.getAttribute("failures").toIntOrNull() ?: 0,
            skipped = root.getAttribute("skipped").toIntOrNull() ?: 0,
            timeSeconds = root.getAttribute("time").toDoubleOrNull() ?: 0.0,
            retried = 0,
        )
    }.getOrNull()

    private fun quote(value: String): String =
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
}

/** Gradle entry point for the postSlackResults task. */
fun main(args: Array<String>) {
    val dir = File(args.firstOrNull() ?: "app/build/test-results")
    SlackNotifier().postFromResults(dir)
}
