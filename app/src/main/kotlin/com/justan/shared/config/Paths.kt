package com.justan.shared.config

import java.nio.file.Path
import java.nio.file.Paths

/** Path rules shared by every config owner: ~ expansion and repo-root relative resolution. */
object Paths {
    private const val ROOT_PROPERTY = "framework.root"

    fun repoRoot(): Path {
        System.getProperty(ROOT_PROPERTY)?.takeIf { it.isNotBlank() }?.let { return Paths.get(it).toAbsolutePath() }
        val cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath()
        return if (cwd.fileName?.toString() == "app") cwd.parent else cwd
    }

    /** Expands a leading ~ and resolves relative paths against the repo root. */
    fun resolve(raw: String): Path {
        if (raw.isBlank()) return Paths.get("")
        val expanded = if (raw == "~" || raw.startsWith("~/")) {
            System.getProperty("user.home") + raw.removePrefix("~")
        } else {
            raw
        }
        val path = Paths.get(expanded)
        return if (path.isAbsolute) path.normalize() else repoRoot().resolve(path).normalize()
    }

    fun resolveString(raw: String): String = resolve(raw).toString()
}
