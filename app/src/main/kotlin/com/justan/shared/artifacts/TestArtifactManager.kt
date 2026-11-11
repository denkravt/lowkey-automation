package com.justan.shared.artifacts

import com.justan.shared.logging.Log
import com.justan.shared.waitstrategy.RetryPolicy
import com.typesafe.config.ConfigFactory
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Finds the APK/IPA to test: a local path wins, otherwise the artifact repo is asked. Downloads are
 * cached in build/downloads so a suite run only fetches once.
 */
class TestArtifactManager(
    private val repoUrl: String = ConfigFactory.load("artifactory.conf").getConfig("artifactory").getString("baseUrl"),
    private val pattern: String = ConfigFactory.load("artifactory.conf").getConfig("artifactory").getString("artifactPattern"),
    private val version: String = ConfigFactory.load("artifactory.conf").getConfig("artifactory").getString("artifactVersion"),
    private val downloadDir: File = File("build/downloads"),
) {
    fun resolve(localPath: String): File {
        val local = File(localPath)
        if (local.exists() && local.length() > 0) {
            Log.info("Using local artifact ${local.absolutePath} (${local.length() / 1024} KB)")
            return local
        }
        check(repoUrl.isNotBlank()) {
            "Artifact not found at $localPath and ARTIFACT_REPO_URL is empty - nothing to install"
        }
        return download()
    }

    private fun download(): File {
        val url = repoUrl.trimEnd('/') + "/" + pattern.replace("%1\$s", version).replace("%2\$s", "android")
        Log.info("Downloading artifact $url")
        downloadDir.mkdirs()
        val target = File(downloadDir, url.substringAfterLast('/'))
        RetryPolicy().execute("download $url") {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 60000
            check(connection.responseCode in 200..299) { "Artifact download failed: HTTP ${connection.responseCode}" }
            connection.inputStream.use { input -> target.outputStream().use { output -> input.copyTo(output) } }
        }
        Log.info("Artifact cached at ${target.absolutePath}")
        return target
    }
}
