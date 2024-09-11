package com.darkrockstudios.apps.hammer.common.components.storyeditor.metadata

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class ProjectMetadata(
	val info: Info
) {
	companion object {
		const val FILENAME = "project.toml"
	}
}

@Serializable
data class Info(
	val created: Instant,
	val lastAccessed: Instant? = null,
	val dataVersion: Int = 0, // Default to 0, if we don't know the version, assume its the oldest
	val serverProjectId: ProjectId? = null,
)

@Serializable
@JvmInline
value class ProjectId(val id: String)