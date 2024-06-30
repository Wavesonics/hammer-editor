package com.darkrockstudios.apps.hammer.projects

import com.darkrockstudios.apps.hammer.account.AccountsComponent
import com.darkrockstudios.apps.hammer.account.AccountsRepository
import com.darkrockstudios.apps.hammer.admin.AdminComponent
import com.darkrockstudios.apps.hammer.admin.WhiteListRepository
import com.darkrockstudios.apps.hammer.base.http.HEADER_SYNC_ID
import com.darkrockstudios.apps.hammer.plugins.configureRouting
import com.darkrockstudios.apps.hammer.plugins.configureSecurity
import com.darkrockstudios.apps.hammer.project.ProjectRepository
import com.darkrockstudios.apps.hammer.utilities.SResult
import com.darkrockstudios.apps.hammer.utils.BaseTest
import com.darkrockstudios.apps.hammer.utils.setupKtorTestKoin
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import io.ktor.server.testing.testApplication
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.serialization.json.Json
import org.junit.Before
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertTrue

class ProjectsRoutesTest : BaseTest() {

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

	private val BEARER_TOKEN = "Bearer token test"

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

	@Test
	fun `Projects - Create Project`() = testApplication {
		coEvery { accountsRepository.checkToken(0L, BEARER_TOKEN) } returns SResult.success(0L)
		coEvery { whiteListRepository.useWhiteList() } returns false

		application {
			setupKtorTestKoin(this@ProjectsRoutesTest, testModule)

			configureSecurity()
			configureRouting()
		}

		client.get("api/projects/0/TestProject/create") {
			header("Authorization", BEARER_TOKEN)
			header(HEADER_SYNC_ID, "test")
		}.apply {
			assertTrue(status.isSuccess())
		}
	}
}

