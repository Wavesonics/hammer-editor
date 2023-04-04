package com.darkrockstudios.apps.hammer.project.synchronizers

import com.darkrockstudios.apps.hammer.base.http.ApiProjectEntity
import com.darkrockstudios.apps.hammer.base.http.synchronizer.EntityConflictException
import com.darkrockstudios.apps.hammer.project.ProjectDefinition
import com.darkrockstudios.apps.hammer.project.ProjectRepository
import com.darkrockstudios.apps.hammer.readJsonOrNull
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okio.FileSystem
import okio.Path
import java.io.IOException
import kotlin.reflect.KClass

abstract class ServerEntitySynchronizer<T : ApiProjectEntity>(
	protected val fileSystem: FileSystem,
	protected val json: Json
) {
	abstract fun hashEntity(entity: T): String
	protected abstract val entityClazz: KClass<T>
	protected abstract val pathStub: String

	protected fun checkForConflict(
		userId: Long,
		projectDef: ProjectDefinition,
		sceneEntity: T,
		originalHash: String?,
		force: Boolean,
	): EntityConflictException? {
		val path = getPath(userId = userId, projectDef = projectDef, entityId = sceneEntity.id)

		return if (!force) {
			if (fileSystem.exists(path)) {
				val existingEntity = fileSystem.readJsonOrNull(path, json, entityClazz)
				if (existingEntity != null) {
					val existingHash = hashEntity(existingEntity)

					if (originalHash != null && existingHash != originalHash) {
						EntityConflictException.fromEntity(existingEntity)
					} else {
						null
					}
				} else {
					null
				}
			} else {
				null
			}
		} else {
			null
		}
	}

	@OptIn(InternalSerializationApi::class)
	fun saveEntity(
		userId: Long,
		projectDef: ProjectDefinition,
		entity: T,
		originalHash: String?,
		force: Boolean,
	): Result<Boolean> {
		val conflict = checkForConflict(
			userId,
			projectDef,
			entity,
			originalHash,
			force,
		)
		return if (conflict == null) {
			try {
				val path = getPath(userId = userId, projectDef = projectDef, entityId = entity.id)
				val jsonString: String = json.encodeToString(entityClazz.serializer(), entity)
				fileSystem.write(path) {
					writeUtf8(jsonString)
				}
				Result.success(true)
			} catch (e: SerializationException) {
				Result.failure(e)
			} catch (e: IllegalArgumentException) {
				Result.failure(e)
			} catch (e: IOException) {
				Result.failure(e)
			}
		} else {
			Result.failure(conflict)
		}
	}

	@OptIn(InternalSerializationApi::class)
	fun loadEntity(
		userId: Long,
		projectDef: ProjectDefinition,
		entityId: Int,
	): Result<T> {
		val path = getPath(userId, projectDef, entityId)

		return try {
			val jsonString = fileSystem.read(path) {
				readUtf8()
			}

			val scene = json.decodeFromString(entityClazz.serializer(), jsonString)
			Result.success(scene)
		} catch (e: SerializationException) {
			Result.failure(e)
		} catch (e: IllegalArgumentException) {
			Result.failure(e)
		} catch (e: IOException) {
			Result.failure(e)
		}
	}

	private fun getPath(userId: Long, projectDef: ProjectDefinition, entityId: Int): Path {
		val entityDir = ProjectRepository.getEntityDirectory(userId, projectDef, fileSystem)
		val filename = "$entityId-$pathStub.json"
		return entityDir / filename
	}

	companion object {
		val ENTITY_FILENAME_REGEX = Regex("^([0-9]+)-([a-zA-Z_]+).json$")

		fun parseEntityFilename(path: Path): EntityDefinition? {
			val filename = path.name
			val match = ENTITY_FILENAME_REGEX.matchEntire(filename)
			return if (match != null) {
				val (id, typeStr) = match.destructured
				val type = ApiProjectEntity.Type.fromString(typeStr)
				if (type != null) {
					EntityDefinition(id.toInt(), type)
				} else {
					null
				}
			} else {
				null
			}
		}
	}
}

data class EntityDefinition(
	val id: Int,
	val type: ApiProjectEntity.Type,
)