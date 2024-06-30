package com.darkrockstudios.apps.hammer.projects

import com.darkrockstudios.apps.hammer.account.AccountsComponent
import com.darkrockstudios.apps.hammer.account.AccountsRepository
import com.darkrockstudios.apps.hammer.admin.AdminComponent
import com.darkrockstudios.apps.hammer.admin.WhiteListRepository
import com.darkrockstudios.apps.hammer.base.http.HEADER_SYNC_ID
import com.darkrockstudios.apps.hammer.plugins.configureRouting
import com.darkrockstudios.apps.hammer.plugins.configureSecurity
import com.darkrockstudios.apps.hammer.plugins.configureSerialization
import com.darkrockstudios.apps.hammer.project.InvalidSyncIdException
import com.darkrockstudios.apps.hammer.project.ProjectRepository
import com.darkrockstudios.apps.hammer.utilities.SResult
import com.darkrockstudios.apps.hammer.utils.BaseTest
import com.darkrockstudios.apps.hammer.utils.setupKtorTestKoin
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.serialization.json.Json
import org.junit.Before
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectsRoutesCreateProjectTest : BaseTest() {

	@MockK(relaxed = true)
	private lateinit var accountsRepository: AccountsRepository

	@MockK(relaxed = true)
	private lateinit var whiteListRepository: WhiteListRepository

	@MockK(relaxed = true)
	private lateinit var projectRepository: ProjectRepository

	@MockK(relaxed = true)
	private lateinit var projectsRepository: ProjectsRepository

	@MockK(relaxed = true)
	private lateinit var accountsComponent: AccountsComponent

	@MockK(relaxed = true)
	private lateinit var adminComponent: AdminComponent

	@MockK(relaxed = true)
	private lateinit var json: Json

	private lateinit var testModule: org.koin.core.module.Module

	private val BEARER_TOKEN = "token-test"

	@Before
	override fun setup() {
		super.setup()

		MockKAnnotations.init(this, relaxUnitFun = true)

		testModule = module {
			single { accountsRepository }
			single { whiteListRepository }
			single { projectRepository }
			single { projectsRepository }
			single { accountsComponent }
			single { adminComponent }
			single { json }
		}
	}

	private fun ApplicationTestBuilder.defaultApplication(moreSetup: (Application.() -> Unit)? = null) {
		application {
			setupKtorTestKoin(this@ProjectsRoutesCreateProjectTest, testModule)

			configureSerialization()
			configureSecurity()
			configureRouting()

			if (moreSetup != null) moreSetup()
		}
	}

	@Test
	fun `Projects - Create Project - Success`() = testApplication {
		val projectName = "TestProject"
		val syncId = "syncId-test"
		val userId = 0L

		coEvery { accountsRepository.checkToken(userId, BEARER_TOKEN) } returns SResult.success(0L)
		coEvery { whiteListRepository.useWhiteList() } returns false
		coEvery {
			projectsRepository.createProject(
				userId = userId,
				syncId = syncId,
				projectName = projectName,
			)
		} returns Result.success(Unit)

		defaultApplication()

		client.get("api/projects/0/TestProject/create") {
			header("Authorization", "Bearer $BEARER_TOKEN")
			header(HEADER_SYNC_ID, syncId)
		}.apply {
			assertTrue(status.isSuccess())
			coVerify {
				projectsRepository.createProject(
					userId = userId,
					syncId = syncId,
					projectName = projectName,
				)
			}
		}
	}

	@Test
	fun `Projects - Create Project - Invalid Request`() = testApplication {
		coEvery { accountsRepository.checkToken(any(), any()) } returns SResult.success(0L)
		coEvery { whiteListRepository.useWhiteList() } returns false

		defaultApplication()

		client.get("api/projects/0/TestProject/create") {
			header("Authorization", "Bearer $BEARER_TOKEN")
		}.apply {
			assertEquals(HttpStatusCode.BadRequest, status)
		}
	}

	@Test
	fun `Projects - Create Project - Failure - Bad SyncId`() = testApplication {
		val projectName = "TestProject"
		val syncId = "syncId-test"
		val userId = 0L

		coEvery { accountsRepository.checkToken(userId, BEARER_TOKEN) } returns SResult.success(0L)
		coEvery { whiteListRepository.useWhiteList() } returns false
		coEvery {
			projectsRepository.createProject(
				userId = userId,
				syncId = syncId,
				projectName = projectName,
			)
		} returns Result.failure(InvalidSyncIdException())

		defaultApplication()

		client.get("api/projects/0/TestProject/create") {
			header("Authorization", "Bearer $BEARER_TOKEN")
			header(HEADER_SYNC_ID, syncId)
		}.apply {
			assertEquals(HttpStatusCode.BadRequest, status)
		}
	}

	@Test
	fun `Projects - Create Project - Failure - Repository Exception`() = testApplication {
		val projectName = "TestProject"
		val syncId = "syncId-test"
		val userId = 0L

		coEvery { accountsRepository.checkToken(userId, BEARER_TOKEN) } returns SResult.success(0L)
		coEvery { whiteListRepository.useWhiteList() } returns false
		coEvery {
			projectsRepository.createProject(
				userId = userId,
				syncId = syncId,
				projectName = projectName,
			)
		} returns Result.failure(Exception())

		defaultApplication()

		client.get("api/projects/0/TestProject/create") {
			header("Authorization", "Bearer $BEARER_TOKEN")
			header(HEADER_SYNC_ID, syncId)
		}.apply {
			assertEquals(HttpStatusCode.InternalServerError, status)
		}
	}
}

