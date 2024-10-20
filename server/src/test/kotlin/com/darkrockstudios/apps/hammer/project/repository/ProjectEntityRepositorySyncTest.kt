package com.darkrockstudios.apps.hammer.project.repository

import com.darkrockstudios.apps.hammer.project.ProjectEntityRepository
import com.darkrockstudios.apps.hammer.project.ProjectSyncData
import com.darkrockstudios.apps.hammer.project.ProjectSynchronizationSession
import com.darkrockstudios.apps.hammer.utilities.isSuccess
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectEntityRepositorySyncTest : ProjectEntityRepositoryBaseTest() {

	@Test
	fun `getProjectSyncData with invalid SyncId`() = runTest {
		coEvery { projectsSessionManager.hasActiveSyncSession(any()) } returns false
		coEvery { projectSessionManager.validateSyncId(any(), any(), any()) } returns false

		ProjectEntityRepository(clock, projectEntityDatasource).apply {
			val result = getProjectSyncData(userId, projectDefinition, "invalid-id")
			assertFalse(result.isSuccess)
		}
	}

	@Test
	fun `Begin Project Sync`() = runTest {
		val syncId = "sync-id"
		val syncData = ProjectSyncData(
			lastSync = clock.now(),
			lastId = 1,
			deletedIds = emptySet()
		)

		coEvery { projectsSessionManager.hasActiveSyncSession(any()) } returns false
		coEvery { projectSessionManager.hasActiveSyncSession(any()) } returns false

		coEvery {
			projectEntityDatasource.checkProjectExists(
				userId,
				projectDefinition
			)
		} returns true

		coEvery {
			projectEntityDatasource.loadProjectSyncData(
				userId,
				projectDefinition
			)
		} returns syncData

		mockCreateSession(syncId)

		createProjectRepository().apply {
			val result = beginProjectSync(userId, projectDefinition, clientState, false)

			assertTrue(isSuccess(result))
			val syncBegan = result.data
			assertTrue(syncBegan.syncId.isNotBlank())
		}
	}

	@Test
	fun `Begin Project Sync - Update Sequence - Remove Dupes`() = runTest {
		val syncId = "sync-id"
		val syncData = ProjectSyncData(
			lastSync = clock.now(),
			lastId = 1,
			deletedIds = emptySet()
		)

		coEvery { projectsSessionManager.hasActiveSyncSession(any()) } returns false
		coEvery { projectSessionManager.hasActiveSyncSession(any()) } returns false

		coEvery {
			projectEntityDatasource.checkProjectExists(
				userId,
				projectDefinition
			)
		} returns true

		coEvery {
			projectEntityDatasource.loadProjectSyncData(
				userId,
				projectDefinition
			)
		} returns syncData

		mockCreateSession(syncId)

		coEvery { sceneSynchronizer.getUpdateSequence(any(), any(), any()) } returns listOf(1, 2, 3)
		coEvery {
			sceneDraftSynchronizer.getUpdateSequence(any(), any(), any())
		} returns listOf(4, 5, 6)
		coEvery { noteSynchronizer.getUpdateSequence(any(), any(), any()) } returns listOf(6, 7, 8)
		coEvery {
			timelineEventSynchronizer.getUpdateSequence(any(), any(), any())
		} returns listOf(8, 9, 10, 11)
		coEvery {
			encyclopediaSynchronizer.getUpdateSequence(any(), any(), any())
		} returns listOf(11, 12, 13, 14)

		createProjectRepository().apply {
			val result = beginProjectSync(userId, projectDefinition, clientState, false)

			assertTrue(isSuccess(result))
			val syncBegan = result.data
			assertTrue(syncBegan.syncId.isNotBlank())
			assertEquals((1..14).toList(), syncBegan.idSequence)
		}
	}

	@Test
	fun `End Project Sync`() = runTest {
		createProjectRepository().apply {

			val syncId = "sync-id"
			val syncData = ProjectSyncData(
				lastSync = clock.now(),
				lastId = 1,
				deletedIds = emptySet()
			)

			coEvery { projectsSessionManager.hasActiveSyncSession(any()) } returns false
			coEvery { projectSessionManager.hasActiveSyncSession(any()) } returns false
			coEvery { projectSessionManager.terminateSession(any()) } returns true

			coEvery {
				projectEntityDatasource.checkProjectExists(
					userId,
					projectDefinition
				)
			} returns true

			coEvery {
				projectEntityDatasource.loadProjectSyncData(
					userId,
					projectDefinition
				)
			} returns syncData

			coEvery {
				projectEntityDatasource.updateSyncData(
					userId,
					projectDefinition,
					any(),
				)
			} just Runs

			mockCreateSession(syncId)

			val beginResult = beginProjectSync(userId, projectDefinition, clientState, false)

			assertTrue(isSuccess(beginResult))

			val syncBegan = beginResult.data
			val session = ProjectSynchronizationSession(
				userId,
				projectDefinition,
				clock.now(),
				syncBegan.syncId
			)
			coEvery { projectSessionManager.findSession(any()) } returns session

			val endResult = endProjectSync(
				userId,
				projectDefinition,
				syncBegan.syncId,
				syncBegan.lastSync,
				syncBegan.lastId
			)
			assertTrue { endResult.isSuccess }
		}
	}

	@Test
	fun `End Project Sync - Invalid SyncId`() = runTest {
		coEvery { projectSessionManager.findSession(any()) } returns null

		createProjectRepository().apply {
			val endResult = endProjectSync(userId, projectDefinition, "invalid-id", null, null)
			assertFalse { endResult.isSuccess }
		}
	}
}