package com.darkrockstudios.apps.hammer.common

expect fun getPlatformName(): String

expect fun getProjectsForDirectory(path: String): List<String>