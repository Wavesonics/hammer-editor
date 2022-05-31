package com.darkrockstudios.apps.hammer.common

import java.io.File

actual fun getPlatformName(): String {
    return "Desktop"
}

actual fun getProjectsForDirectory(path: String): List<String> {
    val dir = File(path)
    return if (dir.exists() && dir.isDirectory) {
        val files = dir.listFiles().filter { it.isDirectory }.map { it.name }
        files.toList()
    } else {
        emptyList()
    }
}