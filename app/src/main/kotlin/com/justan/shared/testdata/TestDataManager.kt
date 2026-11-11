package com.justan.shared.testdata

import com.justan.shared.logging.Log
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File

/** Helpers that install fixture apps and read values from testdata.conf. */
class TestDataManager(private val config: Config = ConfigFactory.load("testdata.conf").getConfig("testdata")) {

    fun recordSeconds(): Int = config.getConfig("recording").getInt("seconds")

    fun cropTimeoutMs(): Long = config.getConfig("recording").getInt("cropTimeoutSeconds") * 1000L

    fun projectName(): String = config.getConfig("project").getString("defaultName")

    fun editableProjectName(): String = config.getConfig("project").getString("editableName")

    fun helperApkInstaller(install: (File) -> Boolean): (File) -> Boolean = { apk ->
        val installed = install(apk)
        Log.info("helper apk ${apk.name} installed=$installed")
        installed
    }
}
