package com.darkrockstudios.apps.hammer.database

import com.darkrockstudios.apps.hammer.project.ProjectDefinition
import com.darkrockstudios.apps.hammer.utilities.injectIoDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class DeletedProjectDao(
	database: Database,
) : KoinComponent {
	private val ioDispatcher by injectIoDispatcher()
	private val queries = database.serverDatabase.deletedProjectQueries

	suspend fun deleteProject(
		userId: Long,
		project: ProjectDefinition,
	) = withContext(ioDispatcher) {
		val alreadyDeleted = queries.hasDeletedProject(
			user_id = userId,
			uuid = project.uuid.id,
		).executeAsOne()

		if (alreadyDeleted.not()) {
			queries.addDeletedProject(
				user_id = userId,
				uuid = project.uuid.id,
				name = project.name,
			)
		}
	}
}