package com.darkrockstudios.apps.hammer.project

import com.darkrockstudios.apps.hammer.base.ProjectId
import com.darkrockstudios.apps.hammer.base.http.ApiProjectEntity
import com.darkrockstudios.apps.hammer.base.http.synchronizer.EntityHasher
import com.darkrockstudios.apps.hammer.database.DeletedProjectDao
import com.darkrockstudios.apps.hammer.database.ProjectDao
import com.darkrockstudios.apps.hammer.database.StoryEntityDao
import com.darkrockstudios.apps.hammer.database.parseDeletedIds
import com.darkrockstudios.apps.hammer.database.parseLastSync
import com.darkrockstudios.apps.hammer.utilities.SResult
import com.darkrockstudios.apps.hammer.utilities.hashEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class ProjectDatabaseDatasource(
	private val projectDao: ProjectDao,
	private val deletedProjectDao: DeletedProjectDao,
	private val storyEntityDao: StoryEntityDao,
	private val json: Json,
) : ProjectDatasource {

	override suspend fun loadProjectSyncData(
		userId: Long,
		projectDef: ProjectDefinition
	): ProjectSyncData {
		val project = projectDao.getProjectData(userId, projectDef.uuid)

		return ProjectSyncData(
			lastSync = project.parseLastSync(),
			lastId = project.last_id.toInt(),
			deletedIds = project.parseDeletedIds()
		)
	}

	override suspend fun createProject(userId: Long, projectName: String): ProjectDefinition {
		val existingProject = projectDao.findProjectData(userId = userId, projectName = projectName)
		val uuid = if (existingProject == null) {
			val newUuid = ProjectId.randomUUID()
			projectDao.createProject(
				userId = userId,
				uuid = newUuid,
				projectName = projectName,
			)
			newUuid
		} else {
			ProjectId(existingProject.uuid)
		}
		return ProjectDefinition(
			name = projectName,
			uuid = uuid,
		)
	}

	override suspend fun deleteProject(userId: Long, project: ProjectDefinition): SResult<Unit> {
		deletedProjectDao.deleteProject(
			userId = userId,
			project = project,
		)

		return SResult.success(Unit)
	}

	override suspend fun checkProjectExists(userId: Long, projectDef: ProjectDefinition): Boolean {
		return projectDao.hasProject(userId, projectDef.name)
	}

	override suspend fun findProjectByName(userId: Long, projectName: String): ProjectDefinition? {
		val project = projectDao.findProjectData(userId, projectName)
		return if (project != null) {
			ProjectDefinition.wrap(
				name = project.name,
				uuid = project.uuid,
			)
		} else {
			null
		}
	}

	override suspend fun updateSyncData(
		userId: Long,
		projectDef: ProjectDefinition,
		action: (ProjectSyncData) -> ProjectSyncData
	) {
		val updated = action(loadProjectSyncData(userId, projectDef))

		projectDao.updateSyncData(
			lastId = updated.lastId.toLong(),
			lastSync = updated.lastSync,
			deletedIds = updated.deletedIds.toList(),
			userId = userId,
			projectName = projectDef.name,
		)
	}

	override suspend fun findLastId(userId: Long, projectDef: ProjectDefinition): Int? {
		val proj = projectDao.getProjectData(userId, projectDef.uuid)
		return storyEntityDao.findMaxId(userId, proj.id)?.toInt()
	}

	override suspend fun findEntityType(
		entityId: Int,
		userId: Long,
		projectDef: ProjectDefinition
	): ApiProjectEntity.Type? {
		val typeId = storyEntityDao.getType(userId, entityId.toLong())
		return ApiProjectEntity.Type.fromInt(typeId.toInt())
	}

	override suspend fun <T : ApiProjectEntity> storeEntity(
		userId: Long,
		projectDef: ProjectDefinition,
		entity: T,
		entityType: ApiProjectEntity.Type,
		serializer: KSerializer<T>
	): SResult<Unit> {
		val projId = projectDao.getProjectData(userId, projectDef.uuid).id
		val hash = EntityHasher.hashEntity(entity)
		val jsonString: String = json.encodeToString(serializer, entity)
		val result = storyEntityDao.upsert(
			userId = userId,
			projectId = projId,
			id = entity.id.toLong(),
			type = entityType.id.toLong(),
			content = jsonString,
			hash = hash,
		)
		return result
	}

	override suspend fun <T : ApiProjectEntity> loadEntity(
		userId: Long,
		projectDef: ProjectDefinition,
		entityId: Int,
		entityType: ApiProjectEntity.Type,
		serializer: KSerializer<T>
	): SResult<T> {
		val projectId = projectDao.getProjectData(userId, projectDef.uuid).id
		val dbEntity = storyEntityDao.getEntity(
			userId = userId,
			projectId = projectId,
			id = entityId.toLong(),
		)

		return if (dbEntity == null) {
			SResult.failure("Entity not found. userId=$userId projectId=$projectId entityId=$entityId")
		} else if (dbEntity.type != entityType.id.toLong()) {
			SResult.failure("Invalid entity type. userId=$userId projectId=$projectId entityId=$entityId entityType=$entityType")
		} else {
			val entity = json.decodeFromString(serializer, dbEntity.content)
			SResult.success(entity)
		}
	}

	override suspend fun deleteEntity(
		userId: Long,
		entityType: ApiProjectEntity.Type,
		projectDef: ProjectDefinition,
		entityId: Int
	): SResult<Unit> {
		val projectId = projectDao.getProjectData(userId, projectDef.uuid).id
		storyEntityDao.deleteEntity(
			userId = userId,
			projectId = projectId,
			id = entityId.toLong(),
		)
		return SResult.success(Unit)
	}

	override suspend fun getEntityDefs(
		userId: Long,
		projectDef: ProjectDefinition,
		filter: (EntityDefinition) -> Boolean
	): List<EntityDefinition> {
		val proj = projectDao.getProjectData(userId, projectDef.uuid)
		return storyEntityDao.getEntityDefs(
			userId = userId,
			projectId = proj.id,
		)
	}
}