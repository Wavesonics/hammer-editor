package com.darkrockstudios.apps.hammer.database

import com.darkrockstudios.apps.hammer.Project
import com.darkrockstudios.apps.hammer.base.ProjectId
import com.darkrockstudios.apps.hammer.project.ProjectDefinition
import com.darkrockstudios.apps.hammer.utilities.injectIoDispatcher
import com.darkrockstudios.apps.hammer.utilities.toSqliteDateTimeString
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent

class ProjectDao(
	database: Database,
	private val clock: Clock,
) : KoinComponent {
	private val ioDispatcher by injectIoDispatcher()
	private val queries = database.serverDatabase.projectQueries

	suspend fun getProjects(userId: Long): Set<ProjectDefinition> = withContext(ioDispatcher) {
		val projects = queries.getProjects(userId).executeAsList()
		return@withContext projects.map { ProjectDefinition.wrap(it.name, it.uuid) }.toSet()
	}

	suspend fun createProject(
		userId: Long,
		uuid: ProjectId,
		projectName: String
	): Boolean = withContext(ioDispatcher) {
		return@withContext try {
			queries.createProject(
				userId = userId,
				name = projectName,
				uuid = uuid.id,
				lastSync = clock.now().toSqliteDateTimeString(),
				lastId = 0,
			)
			true
		} catch (e: Exception) {
			println(e.message)
			false
		}
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
			last_sync = lastSync.toSqliteDateTimeString(),
			deleted_ids = deletedIds.joinToString(","),
			userId,
			projectName,
		)
	}

	suspend fun findProjectData(
		userId: Long,
		projectName: String,
	): Project? = withContext(ioDispatcher) {
		queries.findSyncDataByName(userId, projectName)
			.executeAsOneOrNull()
	}

	suspend fun getProjectData(
		userId: Long,
		projectUuid: ProjectId,
	): Project = withContext(ioDispatcher) {
		queries.getProject(userId, projectUuid.id)
			.executeAsOne()
	}
}