package com.darkrockstudios.apps.hammer.project

import com.darkrockstudios.apps.hammer.base.ProjectId
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDefinition(
	val name: String,
	val uuid: ProjectId
) {
	companion object {
		fun wrap(name: String, uuid: String) = ProjectDefinition(
			name = name,
			uuid = ProjectId(uuid)
		)
	}
}