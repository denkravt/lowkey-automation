package com.justan.shared.logging

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Minimal console logger: frameworks hide enough from you already. */
object Log {
    private val clock = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    private fun prefix(level: String = ""): String =
        "[${LocalTime.now().format(clock)}] [${Thread.currentThread().name}]" + if (level.isEmpty()) "" else " $level"

    fun info(message: String) = println("${prefix()} $message")

    fun warn(message: String) = println("${prefix("WARN")} $message")

    fun error(message: String, throwable: Throwable? = null) {
        println("${prefix("ERROR")} $message")
        throwable?.printStackTrace()
    }

    fun section(title: String) {
        println()
        println("=".repeat(72))
        println("  $title")
        println("=".repeat(72))
    }
}
