package com.darkrockstudios.apps.hammer.base.http

import kotlinx.serialization.Serializable

@Serializable
data class BeginProjectsSyncResponse(
	val syncId: String,
	val projects: Set<String>,
	val deletedProjects: Set<String>,
)

@Serializable
data class CreateProjectResponse(
	val projectId: String,
	val alreadyExisted: Boolean,
)