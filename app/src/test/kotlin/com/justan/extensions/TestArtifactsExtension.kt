package com.justan.extensions

import com.justan.shared.TestRunState
import java.io.File
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext

/** Records failures (so logs/recordings are kept) and screenshots the device on the way out. */
class TestArtifactsExtension : BeforeTestExecutionCallback, AfterTestExecutionCallback {

    override fun beforeTestExecution(context: ExtensionContext) {
        TestRunState.markStarted(key(context))
    }

    override fun afterTestExecution(context: ExtensionContext) {
        val failure = context.executionException.orElse(null) ?: return
        TestRunState.markFailed(key(context))
        val driver = FailureArtifacts.currentDriver()
        val name = "${context.requiredTestClass.simpleName}-${context.requiredTestMethod.name}"
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
        FailureArtifacts.screenshot(driver, com.justan.shared.config.Paths.resolve("build/artifacts/failures/$name.png").toFile())
        println("FAILED ${context.requiredTestMethod.name}: ${failure.message?.lineSequence()?.firstOrNull()}")
    }

    private fun key(context: ExtensionContext): String =
        "${context.requiredTestClass.simpleName}#${context.requiredTestMethod.name}"
}
