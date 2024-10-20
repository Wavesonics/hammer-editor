package repositories.sceneeditor

import PROJECT_1_NAME
import com.darkrockstudios.apps.hammer.common.data.InsertPosition
import com.darkrockstudios.apps.hammer.common.data.MoveRequest
import com.darkrockstudios.apps.hammer.common.data.ProjectDef
import com.darkrockstudios.apps.hammer.common.data.SceneItem
import com.darkrockstudios.apps.hammer.common.data.id.IdRepository
import com.darkrockstudios.apps.hammer.common.data.projectmetadata.ProjectMetadataDatasource
import com.darkrockstudios.apps.hammer.common.data.projectsrepository.ProjectsRepository
import com.darkrockstudios.apps.hammer.common.data.projectsync.ClientProjectSynchronizer
import com.darkrockstudios.apps.hammer.common.data.sceneeditorrepository.SceneDatasource
import com.darkrockstudios.apps.hammer.common.data.sceneeditorrepository.SceneEditorRepository
import com.darkrockstudios.apps.hammer.common.data.sceneeditorrepository.SceneEditorRepositoryOkio
import com.darkrockstudios.apps.hammer.common.data.sceneeditorrepository.scenemetadata.SceneMetadataDatasource
import com.darkrockstudios.apps.hammer.common.data.tree.NodeCoordinates
import com.darkrockstudios.apps.hammer.common.data.tree.Tree
import com.darkrockstudios.apps.hammer.common.data.tree.TreeNode
import com.darkrockstudios.apps.hammer.common.dependencyinjection.createTomlSerializer
import com.darkrockstudios.apps.hammer.common.fileio.HPath
import com.darkrockstudios.apps.hammer.common.fileio.okio.toHPath
import com.darkrockstudios.apps.hammer.common.fileio.okio.toOkioPath
import com.darkrockstudios.apps.hammer.common.getDefaultRootDocumentDirectory
import createProject
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import net.peanuuutz.tomlkt.Toml
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.BaseTest
import utils.getPrivateProperty
import verifyCoords
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SceneEditorRepositoryOkioMoveTest : BaseTest() {

	private lateinit var ffs: FakeFileSystem
	private lateinit var projectPath: HPath
	private lateinit var projectsRepo: ProjectsRepository
	private lateinit var projectSynchronizer: ClientProjectSynchronizer
	private lateinit var projectDef: ProjectDef
	private lateinit var repo: SceneEditorRepository
	private lateinit var idRepository: IdRepository
	private lateinit var metadataRepository: ProjectMetadataDatasource
	private lateinit var metadataDatasource: SceneMetadataDatasource
	private lateinit var sceneDatasource: SceneDatasource
	private var nextId = -1
	private lateinit var toml: Toml

	private fun claimId(): Int {
		val id = nextId
		nextId++
		return id
	}

	private fun verify(
		node: TreeNode<SceneItem>,
		ffs: FakeFileSystem,
		print: Boolean, vararg ids: Int
	) {
		assertEquals(ids.size, node.children().size)

		if (print) {
			node.children().forEachIndexed { index, childNode ->
				println("$index - ${childNode.value.id}")
			}
		}

		node.children().forEachIndexed { index, child ->
			assertEquals(index, child.value.order, "Out of order")
			assertEquals(ids[index], child.value.id, "IDs are out of order")

			// Check mem to filesystem
			val scenePath = repo.getSceneFilePath(child.value.id)
			assertTrue(ffs.exists(scenePath.toOkioPath()))
		}

		// Check file system to mem
		val nodesById = node.children().associateBy({ it.value.id }, { it.value })
		val scenePath = repo.getSceneFilePath(node.value.id)
		ffs.list(scenePath.toOkioPath())
			.filter { it.name != SceneEditorRepository.BUFFER_DIRECTORY }
			.sortedBy { it.name }.forEach { childPath ->
				val sceneItem = repo.getSceneFromPath(childPath.toHPath())
				val foundItem = nodesById[sceneItem.id]
				assertNotNull(sceneItem, "File system scene didn't exist in tree")
				assertEquals(foundItem, sceneItem, "File system scene didn't match tree scene")
			}
	}

	@BeforeEach
	override fun setup() {
		super.setup()
		ffs = FakeFileSystem()

		val rootDir = getDefaultRootDocumentDirectory()
		ffs.createDirectories(rootDir.toPath())

		projectSynchronizer = mockk()
		every { projectSynchronizer.isServerSynchronized() } returns false

		metadataRepository = mockk(relaxed = true)
		metadataDatasource = mockk(relaxed = true)

		projectsRepo = mockk()
		every { projectsRepo.getProjectsDirectory() } returns
				rootDir.toPath().div(PROJ_DIR).toHPath()

		projectPath = projectsRepo.getProjectsDirectory().toOkioPath().div(PROJECT_1_NAME).toHPath()

		projectDef = ProjectDef(
			name = PROJECT_1_NAME,
			path = projectPath
		)
		sceneDatasource = SceneDatasource(projectDef, ffs)

		toml = createTomlSerializer()

		nextId = -1
		idRepository = mockk()
		coEvery { idRepository.claimNextId() } answers { claimId() }
		coEvery { idRepository.findNextId() } answers {}

		createProject(ffs, PROJECT_1_NAME)

		setupKoin()

		repo = SceneEditorRepositoryOkio(
			projectDef = projectDef,
			projectSynchronizer = projectSynchronizer,
			fileSystem = ffs,
			idRepository = idRepository,
			projectMetadataDatasource = metadataRepository,
			sceneMetadataDatasource = metadataDatasource,
			sceneDatasource = sceneDatasource,
		)

		runBlocking {
			repo.initializeSceneEditor()
		}
	}

	@AfterEach
	override fun tearDown() {
		super.tearDown()
		repo.onScopeClose(mockk())

		ffs.checkNoOpenFiles()
	}

	@Test
	fun `Verify Initial Layout`() {
		val tree = repo.getPrivateProperty<SceneEditorRepository, Tree<SceneItem>>("sceneTree")

		for (index in 0..tree.numChildrenRecursive()) {
			assertEquals(index, tree[index].value.id)
		}
	}

	private fun moveTest(
		request: MoveRequest,
		targetPosId: Int,
		leafToVerify: Int,
		print: Boolean,
		vararg ids: Int
	) = runTest {
		val tree = repo.getPrivateProperty<SceneEditorRepository, Tree<SceneItem>>("sceneTree")
		verifyCoords(tree, request.toPosition.coords, targetPosId)
		repo.moveScene(request)

		val afterTree =
			repo.getPrivateProperty<SceneEditorRepository, Tree<SceneItem>>("sceneTree")
		verify(afterTree[leafToVerify], ffs, print, *ids)
	}

	@Test
	fun `Move Scene Sibling, Higher to Lower`() {
		val moveRequest = MoveRequest(
			id = 6,
			toPosition = InsertPosition(
				coords = NodeCoordinates(
					parentIndex = 0,
					childLocalIndex = 0,
					globalIndex = 1
				),
				before = false
			)
		)
		// Initial Order: 1 2 6 7
		moveTest(moveRequest, 1, 0, false, 1, 6, 2, 7)
	}

	@Test
	fun `Move Scene Lower to Higher, After`() {
		val moveRequest = MoveRequest(
			id = 1,
			toPosition = InsertPosition(
				coords = NodeCoordinates(
					parentIndex = 0,
					childLocalIndex = 2,
					globalIndex = 6
				),
				before = false
			)
		)
		// Initial Order: 1, 2, 6, 7
		moveTest(moveRequest, 6, 0, false, 2, 6, 1, 7)
	}

	@Test
	fun `Move to Last, After`() {
		val moveRequest = MoveRequest(
			id = 1,
			toPosition = InsertPosition(
				coords = NodeCoordinates(
					parentIndex = 0,
					childLocalIndex = 3,
					globalIndex = 7
				),
				before = false
			)
		)
		moveTest(moveRequest, 7, 0, false, 2, 6, 7, 1)
	}

	@Test
	fun `Move to Last, Before`() {
		val moveRequest = MoveRequest(
			id = 1,
			toPosition = InsertPosition(
				coords = NodeCoordinates(
					parentIndex = 0,
					childLocalIndex = 3,
					globalIndex = 7
				),
				before = true
			)
		)
		moveTest(moveRequest, 7, 0, false, 2, 6, 1, 7)
	}

	@Test
	fun `Move Scene Lower to Higher, Before`() {
		val moveRequest = MoveRequest(
			id = 6,
			toPosition = InsertPosition(
				coords = NodeCoordinates(
					parentIndex = 0,
					childLocalIndex = 0,
					globalIndex = 1
				),
				before = true
			)
		)
		moveTest(moveRequest, 1, 0, false, 6, 1, 2, 7)
	}

	@Test
	fun `Move Scene Outter to Inner, After`() {
		val moveRequest = MoveRequest(
			id = 6,
			toPosition = InsertPosition(
				coords = NodeCoordinates(
					parentIndex = 2,
					childLocalIndex = 0,
					globalIndex = 3
				),
				before = false
			)
		)
		moveTest(moveRequest, 3, 2, false, 3, 6, 4, 5)
	}

	@Test
	fun `Move Scene Outter to Inner, Before`() {
		val moveRequest = MoveRequest(
			id = 6,
			toPosition = InsertPosition(
				coords = NodeCoordinates(
					parentIndex = 2,
					childLocalIndex = 0,
					globalIndex = 3
				),
				before = true
			)
		)
		// Initial Order: 3, 4, 5
		moveTest(moveRequest, 3, 2, false, 6, 3, 4, 5)
	}

	companion object {
		const val PROJ_DIR = "HammerProjects"
	}
}