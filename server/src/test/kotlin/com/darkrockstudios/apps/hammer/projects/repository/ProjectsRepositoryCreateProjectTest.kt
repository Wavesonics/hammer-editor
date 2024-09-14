package com.darkrockstudios.apps.hammer.projects.repository

import com.darkrockstudios.apps.hammer.project.ProjectDefinition
import com.darkrockstudios.apps.hammer.projects.ProjectsSyncData
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectsRepositoryCreateProjectTest : ProjectsRepositoryBaseTest() {

	@Test
	fun `Create Project, conflicting projects sync session`() = runTest {
		val syncId = "sync-id"
		val projectName = "Project Name"

		coEvery { projectsSessionManager.validateSyncId(any(), any(), any()) } returns false

		createProjectsRepository().apply {
			val result = createProject(userId, syncId, projectName)
			assertTrue(result.isFailure)
			coVerify(exactly = 0) { projectDatasource.createProject(any(), any()) }
			coVerify(exactly = 0) { projectsDatasource.updateSyncData(any(), any()) }
		}
	}

	@Test
	fun `Create Project, successful`() = runTest {
		val syncId = "sync-id"
		val projectName = "Project Name"
		val projectId = "uuid-1"

		coEvery { projectsSessionManager.validateSyncId(userId, syncId, any()) } returns true
		coEvery { projectDatasource.findProjectByName(userId, projectName) } returns null
		coEvery {
			projectDatasource.createProject(
				userId,
				projectName
			)
		} returns ProjectDefinition(projectName, projectId)

		val initialSyncData = ProjectsSyncData(
			lastSync = Instant.DISTANT_PAST,
			deletedProjects = setOf(
				ProjectDefinition(projectName, "uuid-$projectName"),
				ProjectDefinition("Project 2", "uuid-project-2"),
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
				ProjectDefinition("Project 2", "uuid-project-2"),
			),
		)

		createProjectsRepository().apply {
			val result = createProject(userId, syncId, projectName)

			assertTrue(result.isSuccess)
			coVerify { projectDatasource.createProject(userId, projectName) }
			coVerify { projectsDatasource.updateSyncData(userId, any()) }

			assertEquals(expectedSyncData, updatedSyncData)
		}
	}
}