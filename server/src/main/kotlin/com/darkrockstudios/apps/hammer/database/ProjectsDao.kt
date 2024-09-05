package com.darkrockstudios.apps.hammer.database

import com.darkrockstudios.apps.hammer.projects.ProjectsSyncData
import com.darkrockstudios.apps.hammer.utilities.injectIoDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent

class ProjectsDao(
	private val database: Database,
) : KoinComponent {
	private val ioDispatcher by injectIoDispatcher()
	private val syncDataQueries = database.serverDatabase.syncDataQueries
	private val deletedProjectQueries = database.serverDatabase.deletedProjectQueries

	suspend fun getProjectSyncData(userId: Long): ProjectsSyncData = withContext(ioDispatcher) {
		val data = syncDataQueries.getSyncData(userId).executeAsOneOrNull()

		return@withContext if (data != null) {
			val deletedProjects = deletedProjectQueries.getDeletedProjects(userId).executeAsList()
				.map { it.name }
				.toSet()

			ProjectsSyncData(
				lastSync = Instant.parse(data.last_sync),
				deletedProjects = deletedProjects
			)
		} else {
			ProjectsSyncData(
				deletedProjects = emptySet()
			)
		}
	}

	suspend fun updateProjectSyncData(
		userId: Long,
		lastSync: Instant,
		newDeletedProjects: Set<String>
	) = withContext(ioDispatcher) {
		syncDataQueries.updateSyncData(
			last_sync = lastSync.toString(),
			user_id = userId
		)

		// Un-delete a project name
		val currentDeletedProjects = deletedProjectQueries.getDeletedProjects(userId)
			.executeAsList()
			.map { it.name }
		currentDeletedProjects.filterNot {
			newDeletedProjects.contains(it)
		}.forEach {
			deletedProjectQueries.removeDeletedProject(userId, it)
		}

		// Delete newly deleted project names
		newDeletedProjects.filterNot {
			currentDeletedProjects.contains(it)
		}.forEach { projectName ->
			if (deletedProjectQueries.hasDeletedProject(userId, projectName).executeAsOne().not()) {
				deletedProjectQueries.addDeletedProject(userId, projectName)
			}
		}
	}
}