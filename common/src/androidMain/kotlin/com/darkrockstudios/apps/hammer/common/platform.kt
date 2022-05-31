package com.darkrockstudios.apps.hammer.common

actual fun getPlatformName(): String {
    return "Android"
}

actual fun getProjectsForDirectory(path: String): List<String> {
    return listOf("proj 1 android", "proj 1 android")
}