package com.darkrockstudios.apps.hammer.common

import com.darkrockstudios.apps.hammer.common.data.Project

expect fun getPlatformName(): String

expect fun getProjectsForDirectory(path: String): List<Project>