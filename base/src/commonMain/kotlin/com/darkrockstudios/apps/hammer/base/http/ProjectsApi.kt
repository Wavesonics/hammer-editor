package com.darkrockstudios.apps.hammer.base.http

import kotlinx.serialization.Serializable

@Serializable
data class ApiProjectDefinition(
	val name: String,
	val uuid: String,
)

@Serializable
data class BeginProjectsSyncResponse(
	val syncId: String,
	val projects: Set<String>,
	val deletedProjects: Set<ApiProjectDefinition>,
)

@Serializable
data class CreateProjectResponse(
	val projectId: String,
	val alreadyExisted: Boolean,
)