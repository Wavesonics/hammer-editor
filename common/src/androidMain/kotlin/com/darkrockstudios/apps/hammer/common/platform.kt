package com.darkrockstudios.apps.hammer.common

import com.darkrockstudios.apps.hammer.common.data.Project

actual fun getPlatformName(): String {
    return "Android"
}

actual fun getProjectsForDirectory(path: String): List<Project> {
    return listOf(Project("proj 1 android", "/"), Project("proj 1 android", "/"))
}