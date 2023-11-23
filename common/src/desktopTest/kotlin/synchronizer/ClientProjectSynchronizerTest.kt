package synchronizer

import SERVER_SYNC_PROJECT_1
import com.darkrockstudios.apps.hammer.base.http.*
import com.darkrockstudios.apps.hammer.common.data.ProjectDef
import com.darkrockstudios.apps.hammer.common.data.globalsettings.GlobalSettingsRepository
import com.darkrockstudios.apps.hammer.common.data.globalsettings.ServerSettings
import com.darkrockstudios.apps.hammer.common.data.id.IdRepository
import com.darkrockstudios.apps.hammer.common.data.projectbackup.ProjectBackupRepository
import com.darkrockstudios.apps.hammer.common.data.projectsync.ClientProjectSynchronizer
import com.darkrockstudios.apps.hammer.common.data.projectsync.synchronizers.*
import com.darkrockstudios.apps.hammer.common.dependencyinjection.ProjectDefScope
import com.darkrockstudios.apps.hammer.common.server.ServerProjectApi
import com.darkrockstudios.apps.hammer.common.util.NetworkConnectivity
import com.darkrockstudios.apps.hammer.common.util.StrRes
import createProject
import getProject1Def
import getProjectDef
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okio.fakefilesystem.FakeFileSystem
import org.junit.Before
import org.koin.dsl.bind
import org.koin.dsl.module
import utils.BaseTest
import utils.TestStrRes
import utils.testProjectScope
import writeServerFile
import kotlin.test.Test
import kotlin.test.assertTrue

class ClientProjectSynchronizerTest : BaseTest() {

	@MockK(relaxed = true)
	private lateinit var globalSettingsRepository: GlobalSettingsRepository

	@MockK(relaxed = true)
	private lateinit var serverProjectApi: ServerProjectApi

	private lateinit var ffs: FakeFileSystem

	@MockK(relaxed = true)
	private lateinit var idRepository: IdRepository

	@MockK(relaxed = true)
	private lateinit var backupRepository: ProjectBackupRepository

	@MockK(relaxed = true)
	private lateinit var sceneSynchronizer: ClientSceneSynchronizer

	@MockK(relaxed = true)
	private lateinit var noteSynchronizer: ClientNoteSynchronizer

	@MockK(relaxed = true)
	private lateinit var timelineSynchronizer: ClientTimelineSynchronizer

	@MockK(relaxed = true)
	private lateinit var encyclopediaSynchronizer: ClientEncyclopediaSynchronizer

	@MockK(relaxed = true)
	private lateinit var sceneDraftSynchronizer: ClientSceneDraftSynchronizer

	private lateinit var clock: Clock
	private lateinit var strRes: StrRes
	private lateinit var json: Json

	@MockK(relaxed = true)
	private lateinit var networkConnectivity: NetworkConnectivity

	@Before
	override fun setup() {
		super.setup()
		MockKAnnotations.init(this, relaxUnitFun = true)

		ffs = FakeFileSystem()

		json = createJsonSerializer()
		clock = Clock.System
		strRes = TestStrRes()

		val testModule = module {
			single { idRepository } bind IdRepository::class
			single { backupRepository } bind ProjectBackupRepository::class
			single { networkConnectivity } bind NetworkConnectivity::class
			single { strRes } bind StrRes::class
			single { clock } bind Clock::class

			scope<ProjectDefScope> {
				scoped<ProjectDef> { get<ProjectDefScope>().projectDef }

				scoped { sceneSynchronizer } bind ClientSceneSynchronizer::class
				scoped { noteSynchronizer } bind ClientNoteSynchronizer::class
				scoped { timelineSynchronizer } bind ClientTimelineSynchronizer::class
				scoped { encyclopediaSynchronizer } bind ClientEncyclopediaSynchronizer::class
				scoped { sceneDraftSynchronizer } bind ClientSceneDraftSynchronizer::class
			}
		}
		setupKoin(testModule)
	}

	@Test
	fun `ClientProjectSynchronizer initialization`() = runTest {
		val projDef = getProject1Def()
		createProject(ffs, projDef.name)
		writeServerFile(ffs, server_json)

		testProjectScope(projDef) {
			val sync = ClientProjectSynchronizer(
				projectDef = projDef,
				globalSettingsRepository = globalSettingsRepository,
				serverProjectApi = serverProjectApi,
				fileSystem = ffs,
				json = json,
			)
		}
	}

	@Test
	fun `ClientProjectSynchronizer sync`() = runTest {
		val projDef = getProjectDef(SERVER_SYNC_PROJECT_1)
		createProject(ffs, SERVER_SYNC_PROJECT_1)
		writeServerFile(ffs, server_json)

		every { globalSettingsRepository.serverSettingsUpdates } returns flowOf(
			ServerSettings(
				ssl = false,
				url = "127.0.0.1",
				email = "test@test.com",
				userId = 1,
				installId = "asd",
				bearerToken = "asd",
				refreshToken = "asd",
			)
		)

		coEvery {
			serverProjectApi.beginProjectSync(
				userId = any(),
				projectName = SERVER_SYNC_PROJECT_1,
				clientState = any(),
				lite = false
			)
		} returns Result.success(
			ProjectSynchronizationBegan(
				syncId = "1",
				lastSync = Clock.System.now(),
				lastId = 7,
				idSequence = listOf(1),
				deletedIds = emptySet()
			)
		)

		val entityIdSlot = slot<Int>()
		coEvery {
			serverProjectApi.downloadEntity(
				projectDef = projDef,
				entityId = capture(entityIdSlot),
				syncId = any(),
				localHash = any()
			)
		} answers {
			Result.success(
				LoadEntityResponse(
					type = ApiProjectEntity.Type.SCENE,
					entity = getApiEntityForId(entityIdSlot.captured)
				)
			)
		}

		coEvery { sceneSynchronizer.storeEntity(any(), any(), any()) } returns true

		testProjectScope(projDef) {
			val synchronizer = ClientProjectSynchronizer(
				projectDef = projDef,
				globalSettingsRepository = globalSettingsRepository,
				serverProjectApi = serverProjectApi,
				fileSystem = ffs,
				json = json,
			)

			val success = synchronizer.sync(
				onProgress = { progress, log -> },
				onLog = {},
				onConflict = {},
				onComplete = {},
				onlyNew = false
			)

			assertTrue(success)
		}
	}

	companion object {
		val apiEntity1 = ApiProjectEntity.SceneEntity(
			id = 1,
			sceneType = ApiSceneType.Scene,
			order = 0,
			name = "Scene ID 1",
			path = listOf(0),
			content = "content"
		)

		val apiEntity2 = ApiProjectEntity.SceneEntity(
			id = 2,
			sceneType = ApiSceneType.Group,
			order = 0,
			name = "Chapter ID 2",
			path = listOf(0),
			content = ""
		)

		val apiEntity3 = ApiProjectEntity.SceneEntity(
			id = 3,
			sceneType = ApiSceneType.Scene,
			order = 0,
			name = "Scene ID 3",
			path = listOf(0, 2),
			content = "content"
		)

		val apiEntity4 = ApiProjectEntity.SceneEntity(
			id = 4,
			sceneType = ApiSceneType.Scene,
			order = 1,
			name = "Scene ID 4",
			path = listOf(0, 2),
			content = "content"
		)

		val apiEntity5 = ApiProjectEntity.SceneEntity(
			id = 5,
			sceneType = ApiSceneType.Scene,
			order = 2,
			name = "Scene ID 5",
			path = listOf(0, 2),
			content = "content"
		)

		val apiEntity6 = ApiProjectEntity.SceneEntity(
			id = 6,
			sceneType = ApiSceneType.Scene,
			order = 2,
			name = "Scene ID 6",
			path = listOf(0),
			content = "content"
		)

		val apiEntity7 = ApiProjectEntity.SceneEntity(
			id = 7,
			sceneType = ApiSceneType.Scene,
			order = 3,
			name = "Scene ID 7",
			path = listOf(0),
			content = "content"
		)

		fun getApiEntityForId(id: Int): ApiProjectEntity {
			return when (id) {
				1 -> apiEntity1
				2 -> apiEntity2
				3 -> apiEntity3
				4 -> apiEntity4
				5 -> apiEntity5
				6 -> apiEntity6
				7 -> apiEntity7
				else -> error("Unhandled ID")
			}
		}
	}
}

private const val server_json = """{
	"ssl": false,
	"url": "127.0.0.1:8080",
	"email": "test@test.com",
	"userId": 1,
	"installId": "91ac8368-d20f-4436-9983-eb64f33b8bc7",
	"bearerToken": "yP5OoTZC/JlfbyK9KMnrii+Mqpj+P5bdvZIN63IIH213z5a+dPtIq7awF2wrR1wj",
	"refreshToken": "D6UMn50TnGk49o4cjWqOXFv79OpG+fxz2Og5gKU24rGefe01NfBsSI0ktSYq8Hkf"
}"""