package com.darkrockstudios.apps.hammer.database

import com.darkrockstudios.apps.hammer.project.ProjectDefinition
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
				.map {
					ProjectDefinition(
						name = it.name,
						uuid = it.uuid,
					)
				}
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
		newDeletedProjects: Set<ProjectDefinition>
	) = withContext(ioDispatcher) {
		syncDataQueries.updateSyncData(
			last_sync = lastSync.toString(),
			user_id = userId
		)

		// Un-delete a project name
		val currentDeletedProjects = deletedProjectQueries
			.getDeletedProjects(userId)
			.executeAsList()

		currentDeletedProjects.filterNot { currentDeletedProject: DeletedProject ->
			// TODO https://youtrack.jetbrains.com/issue/KT-55239
			newDeletedProjects.contains<Any?> { deletedProject: ProjectDefinition ->
				deletedProject.uuid == currentDeletedProject.uuid
			}
		}.forEach {
			deletedProjectQueries.removeDeletedProject(userId, it.uuid)
		}

		// Delete newly deleted project names
		newDeletedProjects.filterNot { newlyDeleted: ProjectDefinition ->
			// TODO https://youtrack.jetbrains.com/issue/KT-55239
			currentDeletedProjects.contains<Any?> { oldlyDeleted: DeletedProject ->
				oldlyDeleted.uuid == newlyDeleted.uuid
			}
		}.forEach { project ->
			if (deletedProjectQueries.hasDeletedProject(userId, project.uuid).executeAsOne()
					.not()
			) {
				deletedProjectQueries.addDeletedProject(userId, project.name, project.uuid)
			}
		}
	}
}