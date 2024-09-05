package com.darkrockstudios.apps.hammer.database

import com.darkrockstudios.apps.hammer.Project
import kotlinx.datetime.Instant

fun Project.parseDeletedIds(): Set<Int> {
	return deleted_ids?.split(",")?.map { it.toInt() }?.toSet() ?: emptySet()
}

fun Project.parseLastSync(): Instant {
	return Instant.parse(last_sync)
}
