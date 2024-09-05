package com.darkrockstudios.apps.hammer.projects.repository

import com.darkrockstudios.apps.hammer.projects.ProjectsSyncData
import com.darkrockstudios.apps.hammer.utilities.SResult
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectsRepositoryDeleteProjectTest : ProjectsRepositoryBaseTest() {

	@Test
	fun `Delete Project, conflicting projects sync session`() = runTest {
		val syncId = "sync-id"
		val projectName = "Project Name"

		coEvery { projectsSessionManager.validateSyncId(any(), any(), any()) } returns false

		createProjectsRepository().apply {
			val result = deleteProject(userId, syncId, projectName)
			assertTrue(result.isFailure)
			coVerify(exactly = 0) { projectDatasource.deleteProject(any(), any()) }
			coVerify(exactly = 0) { projectsDatasource.updateSyncData(any(), any()) }
		}
	}

	@Test
	fun `Delete Project, successful`() = runTest {
		val syncId = "sync-id"
		val projectName = "Project Name"

		coEvery { projectsSessionManager.validateSyncId(userId, syncId, any()) } returns true
		coEvery { projectDatasource.deleteProject(userId, projectName) } returns SResult.success(
			Unit
		)

		val initialSyncData = ProjectsSyncData(
			lastSync = Instant.DISTANT_PAST,
			deletedProjects = setOf(
				"Project 2"
			),
		)

		var updatedSyncData: ProjectsSyncData? = null
		coEvery { projectsDatasource.updateSyncData(userId, captureLambda()) } answers {
			lambda<(ProjectsSyncData) -> ProjectsSyncData>().captured.invoke(initialSyncData)
				.apply {
					updatedSyncData = this
				}
		}

		val expectedSyncData = ProjectsSyncData(
			lastSync = Instant.DISTANT_PAST,
			deletedProjects = setOf(
				"Project 2",
				projectName,
			),
		)

		createProjectsRepository().apply {
			val result = deleteProject(userId, syncId, projectName)

			assertTrue(result.isSuccess)
			coVerify { projectDatasource.deleteProject(userId, projectName) }
			coVerify { projectsDatasource.updateSyncData(userId, any()) }

			assertEquals(expectedSyncData, updatedSyncData)
		}
	}
}