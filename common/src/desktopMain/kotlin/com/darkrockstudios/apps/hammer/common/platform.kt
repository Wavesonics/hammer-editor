package com.darkrockstudios.apps.hammer.common

import com.darkrockstudios.apps.hammer.common.data.Project
import java.io.File

actual fun getPlatformName(): String {
    return "Desktop"
}

actual fun getProjectsForDirectory(path: String): List<Project> {
    val dir = File(path)
    return if (dir.exists() && dir.isDirectory) {
        val files = dir.listFiles().filter { it.isDirectory }.map { Project(name = it.name, path = it.absolutePath) }
        files.toList()
    } else {
        emptyList()
    }
}