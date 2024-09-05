package com.darkrockstudios.apps.hammer.projects

import com.darkrockstudios.apps.hammer.database.ProjectDao
import com.darkrockstudios.apps.hammer.database.ProjectsDao
import com.darkrockstudios.apps.hammer.project.ProjectDefinition
import com.darkrockstudios.apps.hammer.projects.ProjectsDatasource.Companion.defaultUserData

class ProjectsDatabaseDatasource(
	private val projectDao: ProjectDao,
	private val projectsDao: ProjectsDao,

	) : ProjectsDatasource {

	override suspend fun saveSyncData(userId: Long, data: ProjectsSyncData) {
		projectsDao.updateProjectSyncData(
			userId,
			data.lastSync,
			data.deletedProjects
		)
	}

	override suspend fun getProjects(userId: Long): Set<ProjectDefinition> {
		return projectDao.getProjects(userId)
	}

	override suspend fun loadSyncData(userId: Long): ProjectsSyncData {
		return projectsDao.getProjectSyncData(userId)
	}

	override suspend fun createUserData(userId: Long) {
		val data: ProjectsSyncData = defaultUserData(userId)
		projectsDao.updateProjectSyncData(
			userId,
			data.lastSync,
			data.deletedProjects
		)
	}
}