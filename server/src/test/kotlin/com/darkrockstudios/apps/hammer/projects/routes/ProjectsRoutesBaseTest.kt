package com.darkrockstudios.apps.hammer.projects.routes

import com.darkrockstudios.apps.hammer.account.AccountsComponent
import com.darkrockstudios.apps.hammer.account.AccountsRepository
import com.darkrockstudios.apps.hammer.admin.AdminComponent
import com.darkrockstudios.apps.hammer.admin.WhiteListRepository
import com.darkrockstudios.apps.hammer.plugins.configureRouting
import com.darkrockstudios.apps.hammer.plugins.configureSecurity
import com.darkrockstudios.apps.hammer.plugins.configureSerialization
import com.darkrockstudios.apps.hammer.project.ProjectEntityRepository
import com.darkrockstudios.apps.hammer.projects.ProjectsRepository
import com.darkrockstudios.apps.hammer.utils.BaseTest
import com.darkrockstudios.apps.hammer.utils.setupKtorTestKoin
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.koin.dsl.module

abstract class ProjectsRoutesBaseTest : BaseTest() {

	@MockK(relaxed = true)
	protected lateinit var accountsRepository: AccountsRepository

	@MockK(relaxed = true)
	protected lateinit var whiteListRepository: WhiteListRepository

	@MockK(relaxed = true)
	protected lateinit var projectEntityRepository: ProjectEntityRepository

	@MockK(relaxed = true)
	protected lateinit var projectsRepository: ProjectsRepository

	@MockK(relaxed = true)
	protected lateinit var accountsComponent: AccountsComponent

	@MockK(relaxed = true)
	protected lateinit var adminComponent: AdminComponent

	@MockK(relaxed = true)
	protected lateinit var json: Json

	protected lateinit var testModule: org.koin.core.module.Module

	protected val BEARER_TOKEN = "token-test"

	@BeforeEach
	override fun setup() {
		super.setup()

		MockKAnnotations.init(this, relaxUnitFun = true)

		testModule = module {
			single { accountsRepository }
			single { whiteListRepository }
			single { projectEntityRepository }
			single { projectsRepository }
			single { accountsComponent }
			single { adminComponent }
			single { json }
		}
	}

	protected fun ApplicationTestBuilder.defaultApplication(moreSetup: (Application.() -> Unit)? = null) {
		application {
			setupKtorTestKoin(this@ProjectsRoutesBaseTest, testModule)

			configureSerialization()
			configureSecurity()
			configureRouting()

			if (moreSetup != null) moreSetup()
		}
	}
}

