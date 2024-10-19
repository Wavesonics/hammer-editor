package repositories.sceneeditor

import com.darkrockstudios.apps.hammer.common.components.storyeditor.metadata.Info
import com.darkrockstudios.apps.hammer.common.components.storyeditor.metadata.ProjectMetadata
import com.darkrockstudios.apps.hammer.common.data.ProjectDef
import com.darkrockstudios.apps.hammer.common.data.SceneItem
import com.darkrockstudios.apps.hammer.common.data.id.IdRepository
import com.darkrockstudios.apps.hammer.common.data.migrator.PROJECT_DATA_VERSION
import com.darkrockstudios.apps.hammer.common.data.projectmetadata.ProjectMetadataDatasource
import com.darkrockstudios.apps.hammer.common.data.projectsrepository.ProjectsRepository
import com.darkrockstudios.apps.hammer.common.data.projectsync.ClientProjectSynchronizer
import com.darkrockstudios.apps.hammer.common.data.sceneeditorrepository.SceneEditorRepository
import com.darkrockstudios.apps.hammer.common.data.sceneeditorrepository.SceneEditorRepositoryOkio
import com.darkrockstudios.apps.hammer.common.data.sceneeditorrepository.scenemetadata.SceneMetadataDatasource
import com.darkrockstudios.apps.hammer.common.data.tree.TreeNode
import com.darkrockstudios.apps.hammer.common.dependencyinjection.createTomlSerializer
import com.darkrockstudios.apps.hammer.common.fileio.HPath
import com.darkrockstudios.apps.hammer.common.fileio.okio.toHPath
import com.darkrockstudios.apps.hammer.common.fileio.okio.toOkioPath
import com.darkrockstudios.apps.hammer.common.getDefaultRootDocumentDirectory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import net.peanuuutz.tomlkt.Toml
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.BaseTest
import utils.callPrivate
import kotlin.test.assertEquals

class SceneEditorRepositoryOkioTestSimple : BaseTest() {

	private lateinit var ffs: FakeFileSystem
	private lateinit var projectPath: HPath
	private lateinit var scenesPath: HPath
	private lateinit var projectsRepo: ProjectsRepository
	private lateinit var projectSynchronizer: ClientProjectSynchronizer
	private lateinit var projectDef: ProjectDef
	private lateinit var idRepository: IdRepository
	private lateinit var projectMetadataRepository: ProjectMetadataDatasource
	private lateinit var sceneMetadataDatasource: SceneMetadataDatasource
	private var nextId = -1
	private lateinit var toml: Toml

	private fun claimId(): Int {
		val id = nextId
		nextId++
		return id
	}

	private val sceneFiles = mapOf(
		"1-Scene 1-1.md" to "This is scene 1 content",
		"2-Scene 2-2.md" to "This is scene 2 content",
		"3-Scene 3-3.md" to "This is scene 3 content"
	)

	private val projectMetadata = ProjectMetadata(
		info = Info(
			created = Instant.DISTANT_FUTURE,
			lastAccessed = Instant.DISTANT_FUTURE,
			dataVersion = PROJECT_DATA_VERSION,
		)
	)

	private fun populateProject(fs: FakeFileSystem) {
		fs.createDirectories(projectPath.toOkioPath())
		scenesPath = projectPath.toOkioPath().div(SceneEditorRepository.SCENE_DIRECTORY).toHPath()

		fs.createDirectory(scenesPath.toOkioPath())

		sceneFiles.entries.forEach { (name, content) ->
			val path = scenesPath.toOkioPath().div(name)
			fs.write(path) {
				writeUtf8(content)
			}
		}
		assertEquals(sceneFiles.size, fs.list(scenesPath.toOkioPath()).size)
	}

	private fun scenePath(filename: String): HPath {
		return scenesPath.toOkioPath().div(filename).toHPath()
	}

	@BeforeEach
	override fun setup() {
		super.setup()
		ffs = FakeFileSystem()

		val rootDir = getDefaultRootDocumentDirectory()
		ffs.createDirectories(rootDir.toPath())

		projectMetadataRepository = mockk(relaxed = true)
		every { projectMetadataRepository.loadMetadata(any()) } returns projectMetadata

		sceneMetadataDatasource = mockk(relaxed = true)

		projectsRepo = mockk()
		every { projectsRepo.getProjectsDirectory() } returns
				rootDir.toPath().div(PROJ_DIR).toHPath()

		projectPath = projectsRepo.getProjectsDirectory().toOkioPath().div(PROJ_NAME).toHPath()

		toml = createTomlSerializer()

		nextId = -1
		idRepository = mockk()
		coEvery { idRepository.claimNextId() } answers { claimId() }
		coEvery { idRepository.findNextId() } answers {}

		populateProject(ffs)

		projectDef = ProjectDef(
			name = PROJ_NAME,
			path = projectPath
		)

		projectSynchronizer = mockk()
		every { projectSynchronizer.isServerSynchronized() } returns false

		setupKoin()
	}

	@AfterEach
	override fun tearDown() {
		super.tearDown()
		ffs.checkNoOpenFiles()
	}

	@Test
	fun `Get filename`() {
		val repo = SceneEditorRepositoryOkio(
			projectDef = projectDef,
			projectSynchronizer = projectSynchronizer,
			fileSystem = ffs,
			idRepository = idRepository,
			projectMetadataDatasource = projectMetadataRepository,
			sceneMetadataDatasource = sceneMetadataDatasource,
		)

		val expectedFilename = sceneFiles.entries.first().key
		val scenePath = scenePath(expectedFilename)
		val sceneFilename = repo.getSceneFilename(scenePath)
		assertEquals(expectedFilename, sceneFilename)
	}

	@Test
	fun `Load Scene Tree`() {
		val repo = SceneEditorRepositoryOkio(
			projectDef = projectDef,
			projectSynchronizer = projectSynchronizer,
			fileSystem = ffs,
			idRepository = idRepository,
			projectMetadataDatasource = projectMetadataRepository,
			sceneMetadataDatasource = sceneMetadataDatasource,
		)

		val sceneTree: TreeNode<SceneItem> = repo.callPrivate("loadSceneTree")

		assertEquals(3, sceneTree.numChildrenRecursive())
		assertEquals(1, sceneTree[0].value.id)
		assertEquals(2, sceneTree[1].value.id)
		assertEquals(3, sceneTree[2].value.id)
	}

	@Test
	fun `Init Editor`() = runTest {
		val repo = SceneEditorRepositoryOkio(
			projectDef = projectDef,
			projectSynchronizer = projectSynchronizer,
			fileSystem = ffs,
			idRepository = idRepository,
			projectMetadataDatasource = projectMetadataRepository,
			sceneMetadataDatasource = sceneMetadataDatasource,
		)

		repo.initializeSceneEditor()

		val metadata = repo.getMetadata()

		assertEquals(projectMetadata, metadata)

		repo.onScopeClose(mockk())
	}

	companion object {
		const val PROJ_DIR = "HammerProjects"
		const val PROJ_NAME = "Test Proj"
	}
}