import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    `java-library`
    id("org.gradle.test-retry") version "1.6.0"
}

group = "com.justan"
version = "0.1.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(libs.appium.java.client)
    api(libs.typesafe.config)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    // Default `test`/`build` is device-free; device work lives in the named suite tasks.
    useJUnitPlatform {
        includeTags("PR-CHECK")
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt.yml"))
    source.setFrom(
        files(
            "$rootDir/app/src/main/kotlin",
            "$rootDir/app/src/test/kotlin",
        ),
    )
}

tasks.withType<Test>().configureEach {
    // JUnit extension autodetection so platform extensions register themselves
    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    // Forward the -D flags the runners/documentation advertise
    System.getProperties().forEach { key, value ->
        val k = key as? String ?: return@forEach
        val v = value as? String ?: return@forEach
        if (k.startsWith("appium.") || k.startsWith("device.") || k.startsWith("skip") || k == "screenshotOnFailure") {
            systemProperty(k, v)
        }
    }
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    outputs.upToDateWhen { false }
    retry {
        maxRetries.set(1)
        maxFailures.set(40)
        failOnPassedAfterRetry.set(false)
    }
}

/** Registers a named suite task that selects tests by JUnit tag expression. */
fun configureTestTask(taskName: String, tagExpression: String, descriptionText: String) {
    tasks.register<Test>(taskName) {
        group = "verification"
        description = descriptionText
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
        useJUnitPlatform {
            includeTags(tagExpression)
            // Probe suites (DEBUG) never run as part of a real suite.
            if (tagExpression != "DEBUG") excludeTags("DEBUG")
        }
        filter.isFailOnNoMatchingTests = false
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        System.getProperties().forEach { key, value ->
            val k = key as? String ?: return@forEach
            val v = value as? String ?: return@forEach
            if (k.startsWith("appium.") || k.startsWith("device.") || k.startsWith("skip") || k == "screenshotOnFailure") {
                systemProperty(k, v)
            }
        }
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        outputs.upToDateWhen { false }
        retry {
            maxRetries.set(1)
            maxFailures.set(40)
            failOnPassedAfterRetry.set(false)
        }
    }
}

configureTestTask("lowkey-android", "LOWKEY & ANDROID", "Every LowKey Android test")
configureTestTask("lowkey-android-smoke", "LOWKEY & ANDROID & SMOKE", "LowKey Android smoke suite")
configureTestTask("lowkey-android-t0", "LOWKEY & ANDROID & T0", "LowKey Android T0 suite")
configureTestTask("lowkey-ios", "LOWKEY & IOS", "Every LowKey iOS test (needs a device + WDA)")
configureTestTask("lowkey-ios-smoke", "LOWKEY & IOS & SMOKE", "LowKey iOS smoke suite")
configureTestTask("pr", "PR-CHECK", "Fast gate for pull requests - no device required")
configureTestTask("debug-suite", "DEBUG", "Harness self-checks: session smoke and failure-artifact probe")

/**
 * Flaky tests may be disabled, but only with a ticket reference so we never lose track of them.
 * Usage: @Disabled("LOCAL-12 playback stalls on Android 17")
 */
tasks.register("verifyTestAnnotations") {
    group = "verification"
    description = "Fails when @Disabled does not cite a ticket id."
    val testSources = fileTree("$rootDir/app/src/test/kotlin") { include("**/*.kt") }
    inputs.files(testSources)
    doLast {
        val ticket = Regex("""[A-Z][A-Z0-9_]+-\d+""")
        val disabled = Regex("""@Disabled\s*\(([^)]*)\)""")
        val offenders = mutableListOf<String>()
        testSources.forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                disabled.find(line)?.let { match ->
                    if (!ticket.containsMatchIn(match.groupValues[1])) {
                        offenders += "${file.relativeTo(rootDir)}:${index + 1} -> ${line.trim()}"
                    }
                }
            }
        }
        if (offenders.isNotEmpty()) {
            throw GradleException(
                "Every @Disabled needs a ticket reference (e.g. LOCAL-12):\n" + offenders.joinToString("\n"),
            )
        }
        println("verifyTestAnnotations: all @Disabled annotations cite a ticket.")
    }
}

tasks.register<JavaExec>("postSlackResults") {
    group = "reporting"
    description = "Summarises JUnit XML in build/test-results and posts it to Slack (dry-run without a token)."
    mainClass.set("com.justan.shared.ci.SlackNotifierKt")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("$rootDir/app/build/test-results")
}
