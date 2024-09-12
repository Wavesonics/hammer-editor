package com.darkrockstudios.apps.hammer.database

import com.darkrockstudios.apps.hammer.Project
import com.darkrockstudios.apps.hammer.project.ProjectDefinition
import com.darkrockstudios.apps.hammer.utilities.injectIoDispatcher
import com.darkrockstudios.apps.hammer.utilities.toISO8601
import korlibs.io.util.UUID
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent

class ProjectDao(
	database: Database,
) : KoinComponent {
	private val ioDispatcher by injectIoDispatcher()
	private val queries = database.serverDatabase.projectQueries

	suspend fun getProjects(userId: Long): Set<ProjectDefinition> = withContext(ioDispatcher) {
		val projects = queries.getProjects(userId).executeAsList()
		return@withContext projects.map { ProjectDefinition(it.name, it.uuid) }.toSet()
	}

	suspend fun createProject(
		userId: Long,
		uuid: UUID,
		projectName: String
	) = withContext(ioDispatcher) {
		queries.createProject(
			user_id = userId,
			name = projectName,
			uuid = uuid.toString(),
			last_id = 0,
		)
	}

	suspend fun hasProject(userId: Long, projectName: String): Boolean = withContext(ioDispatcher) {
		queries.hasProject(userId, projectName).executeAsOne()
	}

	suspend fun updateProjectName(
		userId: Long,
		oldName: String,
		newName: String
	) = withContext(ioDispatcher) {
		queries.updateProjectName(newName, userId, oldName)
	}

	suspend fun updateSyncData(
		lastId: Long,
		lastSync: Instant,
		deletedIds: List<Int>,
		userId: Long,
		projectName: String,
	) = withContext(ioDispatcher) {
		queries.updateSyncData(
			last_id = lastId,
			last_sync = lastSync.toISO8601(),
			deleted_ids = deletedIds.joinToString(","),
			userId,
			projectName,
		)
	}

	suspend fun findProjectData(
		userId: Long,
		projectName: String,
	): Project? = withContext(ioDispatcher) {
		queries.getSyncDataByName(userId, projectName)
			.executeAsOneOrNull()
	}

	suspend fun getProjectData(
		userId: Long,
		projectUuid: String,
	): Project = withContext(ioDispatcher) {
		queries.getSyncData(userId, projectUuid)
			.executeAsOne()
	}
}