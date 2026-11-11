package com.justan.extensions

import com.justan.shared.logging.Log
import java.io.File
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Suite setup failures (Appium won't start, APK missing, device gone) still produce evidence:
 * a screenshot when a session exists and a text dump of the reason.
 */
class BeforeAllFailureCaptureExtension : AfterAllCallback {

    override fun afterAll(context: ExtensionContext) {
        val failure = context.executionException.orElse(null) ?: return
        val dir = com.justan.shared.config.Paths.resolve("build/artifacts/failures").toFile().apply { mkdirs() }
        FailureArtifacts.screenshot(FailureArtifacts.currentDriver(), File(dir, "beforeAll-${context.requiredTestClass.simpleName}.png"))
        val dump = File(dir, "beforeAll-${context.requiredTestClass.simpleName}.txt")
        dump.writeText(
            buildString {
                appendLine("class: ${context.requiredTestClass.name}")
                appendLine("failure: ${failure.javaClass.name}")
                appendLine(failure.stackTraceToString())
            },
        )
        Log.error("suite setup failed for ${context.requiredTestClass.simpleName}, evidence in ${dir.absolutePath}", failure)
    }
}
