package com.darkrockstudios.apps.hammer.dependencyinjection

import com.darkrockstudios.apps.hammer.account.AccountsComponent
import com.darkrockstudios.apps.hammer.account.AccountsRepository
import com.darkrockstudios.apps.hammer.admin.AdminComponent
import com.darkrockstudios.apps.hammer.admin.WhiteListRepository
import com.darkrockstudios.apps.hammer.base.http.createJsonSerializer
import com.darkrockstudios.apps.hammer.database.AccountDao
import com.darkrockstudios.apps.hammer.database.AuthTokenDao
import com.darkrockstudios.apps.hammer.database.Database
import com.darkrockstudios.apps.hammer.database.DeletedEntityDao
import com.darkrockstudios.apps.hammer.database.DeletedProjectDao
import com.darkrockstudios.apps.hammer.database.ProjectDao
import com.darkrockstudios.apps.hammer.database.ProjectsDao
import com.darkrockstudios.apps.hammer.database.SqliteDatabase
import com.darkrockstudios.apps.hammer.database.StoryEntityDao
import com.darkrockstudios.apps.hammer.database.WhiteListDao
import com.darkrockstudios.apps.hammer.encryption.AesContentEncryptor
import com.darkrockstudios.apps.hammer.encryption.AesKeyProvider
import com.darkrockstudios.apps.hammer.encryption.ContentEncryptor
import com.darkrockstudios.apps.hammer.encryption.SimpleAesKeyProvider
import com.darkrockstudios.apps.hammer.project.ProjectDatabaseDatasource
import com.darkrockstudios.apps.hammer.project.ProjectDatasource
import com.darkrockstudios.apps.hammer.project.ProjectRepository
import com.darkrockstudios.apps.hammer.project.ProjectSyncKey
import com.darkrockstudios.apps.hammer.project.ProjectSynchronizationSession
import com.darkrockstudios.apps.hammer.project.synchronizers.ServerEncyclopediaSynchronizer
import com.darkrockstudios.apps.hammer.project.synchronizers.ServerNoteSynchronizer
import com.darkrockstudios.apps.hammer.project.synchronizers.ServerSceneDraftSynchronizer
import com.darkrockstudios.apps.hammer.project.synchronizers.ServerSceneSynchronizer
import com.darkrockstudios.apps.hammer.project.synchronizers.ServerTimelineSynchronizer
import com.darkrockstudios.apps.hammer.projects.ProjectsDatabaseDatasource
import com.darkrockstudios.apps.hammer.projects.ProjectsDatasource
import com.darkrockstudios.apps.hammer.projects.ProjectsRepository
import com.darkrockstudios.apps.hammer.projects.ProjectsSynchronizationSession
import com.darkrockstudios.apps.hammer.syncsessionmanager.SyncSessionManager
import io.ktor.util.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okio.FileSystem
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

const val DISPATCHER_MAIN = "main-dispatcher"
const val DISPATCHER_DEFAULT = "default-dispatcher"
const val DISPATCHER_IO = "io-dispatcher"

fun mainModule(
	logger: Logger,
) = module {
	single<CoroutineContext>(named(DISPATCHER_MAIN)) { Dispatchers.Unconfined }
	single<CoroutineContext>(named(DISPATCHER_DEFAULT)) { Dispatchers.Default }
	single<CoroutineContext>(named(DISPATCHER_IO)) { Dispatchers.IO }

	single { logger }

	singleOf(::createJsonSerializer) bind Json::class
	single { Clock.System } bind Clock::class

	single { FileSystem.SYSTEM } bind FileSystem::class
	singleOf(::SqliteDatabase) bind Database::class
	singleOf(::AccountDao)
	singleOf(::AuthTokenDao)
	singleOf(::WhiteListDao)
	singleOf(::StoryEntityDao)
	singleOf(::ProjectsDao)
	singleOf(::ProjectDao)
	singleOf(::DeletedProjectDao)
	singleOf(::DeletedEntityDao)

	singleOf(::AccountsRepository)
	singleOf(::ProjectsRepository)
	singleOf(::ProjectRepository)
	singleOf(::WhiteListRepository)

	singleOf(::SimpleAesKeyProvider) bind AesKeyProvider::class
	singleOf(::AesContentEncryptor) bind ContentEncryptor::class

	factoryOf(::ProjectsDatabaseDatasource) bind ProjectsDatasource::class
	factoryOf(::ProjectDatabaseDatasource) bind ProjectDatasource::class

	singleOf(::AdminComponent)
	singleOf(::AccountsComponent)

	singleOf(::ServerSceneSynchronizer)
	singleOf(::ServerNoteSynchronizer)
	singleOf(::ServerTimelineSynchronizer)
	singleOf(::ServerEncyclopediaSynchronizer)
	singleOf(::ServerSceneDraftSynchronizer)

	single<SyncSessionManager<Long, ProjectsSynchronizationSession>>(named(PROJECTS_SYNC_MANAGER)) {
		SyncSessionManager(get())
	}

	single<SyncSessionManager<ProjectSyncKey, ProjectSynchronizationSession>>(
		named(
			PROJECT_SYNC_MANAGER
		)
	) {
		SyncSessionManager(get())
	}
}

const val PROJECTS_SYNC_MANAGER = "projects_sync_manager"
const val PROJECT_SYNC_MANAGER = "project_sync_manager"