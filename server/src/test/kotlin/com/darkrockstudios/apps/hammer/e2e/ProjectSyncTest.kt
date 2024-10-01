package com.darkrockstudios.apps.hammer.e2e

import com.darkrockstudios.apps.hammer.base.http.ApiProjectEntity
import com.darkrockstudios.apps.hammer.base.http.ClientEntityState
import com.darkrockstudios.apps.hammer.base.http.EntityHash
import com.darkrockstudios.apps.hammer.base.http.synchronizer.EntityHasher
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.createAuthToken
import com.darkrockstudios.apps.hammer.e2e.util.TestDataSet1
import com.darkrockstudios.apps.hammer.utilities.hashEntity
import com.darkrockstudios.apps.hammer.utils.SERVER_EMPTY_NO_WHITELIST
import com.darkrockstudios.apps.hammer.utils.createTestServer
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectSyncTest : ProjectSyncTestBase() {

	@Test
	fun `A full ProjectSync where all entities are already up to date`(): Unit = runBlocking {
		val database = database()
		createTestServer(SERVER_EMPTY_NO_WHITELIST, fileSystem, database)
		TestDataSet1.createFullDataset(database)
		val userId = 1L
		val authToken = createAuthToken(userId, "test-install-id", database = database)
		doStartServer()

		val state = ClientEntityState(
			entities = TestDataSet1.user1Project1Entities.map { entity ->
				EntityHash(
					id = entity.id,
					hash = EntityHasher.hashEntity(entity)
				)
			}.toSet()
		)

		client().apply {
			// Begin Sync
			val synchronizationBegan = projectSynchronizationBegan(userId, authToken, state)

			println(synchronizationBegan)
			assertEquals(emptyList(), synchronizationBegan.idSequence)
			assertEquals(setOf(7), synchronizationBegan.deletedIds)

			endSyncRequest(userId, authToken, synchronizationBegan)
		}
	}

	@Test
	fun `A full ProjectSync where one entity is not up to date`(): Unit = runBlocking {
		val database = database()
		createTestServer(SERVER_EMPTY_NO_WHITELIST, fileSystem, database)
		TestDataSet1.createFullDataset(database)
		val userId = 1L
		val authToken = createAuthToken(userId, "test-install-id", database = database)
		doStartServer()

		val entityIdMissingFromClient = 2
		val entityIdToUpdate = 3

		val state = ClientEntityState(
			entities = TestDataSet1.user1Project1Entities
				.filter { entity -> entity.id != entityIdMissingFromClient }
				.map { entity ->
				if (entity.id == entityIdToUpdate) {
					EntityHash(
						id = entity.id,
						hash = "not-up-to-date"
					)
				} else {
					EntityHash(
						id = entity.id,
						hash = EntityHasher.hashEntity(entity)
					)
				}
			}.toSet()
		)

		client().apply {
			// Begin Sync
			val synchronizationBegan = projectSynchronizationBegan(userId, authToken, state)

			println(synchronizationBegan)
			assertEquals(
				listOf(entityIdMissingFromClient, entityIdToUpdate),
				synchronizationBegan.idSequence
			)
			assertEquals(setOf(7), synchronizationBegan.deletedIds)

			// Now walk the sequence and process entities
			synchronizationBegan.idSequence.forEach { entityId ->
				val entity = TestDataSet1.user1Project1Entities.find { it.id == entityId }!!
				when (entityId) {
					entityIdMissingFromClient -> {
						entity as ApiProjectEntity.SceneEntity
						val downloadResponse = downloadEntityRequest(
							userId,
							authToken,
							synchronizationBegan.syncId,
							entity,
							null,
						)
						// TODO do better asserting here
						assertEquals(entity.id, downloadResponse.id)
					}

					entityIdToUpdate -> {
						entity as ApiProjectEntity.SceneEntity
						val modifiedEntity = entity.copy(
							name = "updated"
						)
						val uploadResponse = uploadEntityRequest(
							userId,
							authToken,
							synchronizationBegan.syncId,
							modifiedEntity,
							EntityHasher.hashEntity(entity),
						)
						assertTrue(uploadResponse.saved)
					}
				}
			}

			endSyncRequest(userId, authToken, synchronizationBegan)
		}
	}
}