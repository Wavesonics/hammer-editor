package com.darkrockstudios.apps.hammer.common.components.projectselection.projectslist

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.getAndUpdate
import com.darkrockstudios.apps.hammer.common.components.ComponentBase
import com.darkrockstudios.apps.hammer.common.components.projecteditor.metadata.ProjectMetadata
import com.darkrockstudios.apps.hammer.common.components.projectselection.ProjectData
import com.darkrockstudios.apps.hammer.common.data.ProjectDef
import com.darkrockstudios.apps.hammer.common.data.globalsettings.GlobalSettingsRepository
import com.darkrockstudios.apps.hammer.common.data.projectsrepository.ProjectsRepository
import com.darkrockstudios.apps.hammer.common.data.projectsync.ClientProjectSynchronizer
import com.darkrockstudios.apps.hammer.common.data.projectsync.ClientProjectsSynchronizer
import com.darkrockstudios.apps.hammer.common.data.temporaryProjectTask
import com.darkrockstudios.apps.hammer.common.dependencyinjection.injectMainDispatcher
import com.darkrockstudios.apps.hammer.common.fileio.HPath
import com.darkrockstudios.apps.hammer.common.fileio.okio.toHPath
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import okio.Path.Companion.toPath
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.collections.set

class ProjectsListComponent(
	componentContext: ComponentContext,
	private val onProjectSelected: (projectDef: ProjectDef) -> Unit
) : ProjectsList, ComponentBase(componentContext) {

	private val mainDispatcher by injectMainDispatcher()

	private val globalSettingsRepository: GlobalSettingsRepository by inject()
	private val projectsRepository: ProjectsRepository by inject()
	private val projectsSynchronizer: ClientProjectsSynchronizer by inject()

	private var loadProjectsJob: Job? = null
	private var syncProjectsJob: Job? = null

	private val _state = MutableValue(
		ProjectsList.State(
			projects = emptyList(),
			projectsPath = HPath(globalSettingsRepository.globalSettings.projectsDirectory, "", true),
			isServerSynced = projectsSynchronizer.isServerSynchronized(),
		)
	)
	override val state: Value<ProjectsList.State> = _state

	private fun watchSettingsUpdates() {
		scope.launch {
			globalSettingsRepository.globalSettingsUpdates.collect { settings ->
				withContext(dispatcherMain) {
					val oldPath = state.value.projectsPath
					_state.getAndUpdate {
						val projectsPath = settings.projectsDirectory.toPath().toHPath()
						it.copy(
							projectsPath = projectsPath,
						)
					}

					if (oldPath.path != settings.projectsDirectory) {
						loadProjectList()
					}
				}
			}
		}

		scope.launch {
			globalSettingsRepository.serverSettingsUpdates.collect { settings ->
				withContext(dispatcherMain) {
					_state.getAndUpdate {
						it.copy(
							isServerSynced = settings?.url != null,
						)
					}
				}
			}
		}
	}

	override fun onCreate() {
		super.onCreate()
		watchSettingsUpdates()
		loadProjectList()
	}

	override fun onStart() {
		super.onStart()

		_state.getAndUpdate {
			it.copy(
				projectsPath = HPath(globalSettingsRepository.globalSettings.projectsDirectory, "", true),
				isServerSynced = projectsSynchronizer.isServerSynchronized()
			)
		}
	}

	override fun loadProjectList() {
		val projectsDir = HPath(
			path = globalSettingsRepository.globalSettings.projectsDirectory,
			name = "",
			isAbsolute = true
		)

		loadProjectsJob?.cancel()
		loadProjectsJob = scope.launch {
			val projects = projectsRepository.getProjects(projectsDir)
			val projectData = projects.mapNotNull { projectDef ->
				val metadata = projectsRepository.loadMetadata(projectDef)
				if (metadata != null) {
					ProjectData(projectDef, metadata)
				} else {
					Napier.w { "Failed to load metadata for project: ${projectDef.name}" }
					null
				}
			}

			withContext(dispatcherMain) {
				_state.getAndUpdate {
					it.copy(projects = projectData)
				}
				loadProjectsJob = null
			}
		}
	}

	override fun selectProject(projectDef: ProjectDef) = onProjectSelected(projectDef)

	override fun createProject(projectName: String) {
		if (projectsRepository.createProject(projectName)) {
			if (projectsSynchronizer.isServerSynchronized()) {
				projectsSynchronizer.createProject(projectName)
			}
			Napier.i("Project created: $projectName")
			loadProjectList()
		} else {
			Napier.e("Failed to create Project: $projectName")
		}
	}

	override fun deleteProject(projectDef: ProjectDef) {
		if (projectsRepository.deleteProject(projectDef)) {
			Napier.i("Project deleted: ${projectDef.name}")
			if (projectsSynchronizer.isServerSynchronized()) {
				projectsSynchronizer.deleteProject(projectDef)
			}

			loadProjectList()
		}
	}

	override suspend fun loadProjectMetadata(projectDef: ProjectDef): ProjectMetadata? {
		return projectsRepository.loadMetadata(projectDef)
	}

	private suspend fun syncProject(
		projectDef: ProjectDef,
		onLog: suspend (String) -> Unit,
		onProgress: suspend (Float, String?) -> Unit
	): Boolean {
		onLog("Syncing Project: ${projectDef.name}")

		var success = false
		temporaryProjectTask(projectDef) { projScope ->
			val synchronizer: ClientProjectSynchronizer = projScope.get { parametersOf(projectDef) }
			success = synchronizer.sync(
				onProgress = onProgress,
				onLog = { message -> message?.let { onLog(it) } },
				onConflict = {
					onLog("There is a conflict in project: ${projectDef.name}, open that project and sync in order to resolve it")
					throw IllegalStateException("Entity conflict must be handled by Project sync")
				},
				onComplete = {}
			)
		}

		return success
	}

	private fun syncNewProjectStatus(projects: List<ProjectDef>) {
		val newStatuses = mutableMapOf<String, ProjectsList.ProjectSyncStatus>()
		projects.forEach { projDef ->
			newStatuses[projDef.name] = ProjectsList.ProjectSyncStatus(projectName = projDef.name)
		}

		_state.getAndUpdate {
			it.copy(
				syncState = it.syncState.copy(
					projectsStatus = newStatuses
				)
			)
		}
	}

	private fun syncProgressStatus(projectName: String, status: ProjectsList.Status, progress: Float? = null) {
		_state.getAndUpdate {
			val projStatus = it.syncState.projectsStatus[projectName]!!

			val map = it.syncState.projectsStatus.toMutableMap()
			val updatedMap = projStatus.copy(
				status = status,
				progress = progress ?: projStatus.progress
			)
			map[projectName] = updatedMap

			it.copy(
				syncState = it.syncState.copy(
					projectsStatus = map
				)
			)
		}
	}

	override fun syncProjects(callback: (Boolean) -> Unit) {
		syncProjectsJob?.cancel(CancellationException("Started another sync"))
		syncProjectsJob = scope.launch {
			val projects = projectsRepository.getProjects()

			syncNewProjectStatus(projects)

			onSyncLog("Syncing Account...")

			val success = projectsSynchronizer.syncProjects(::onSyncLog)

			yield()

			var allSuccess = success
			if (success) {
				onSyncLog("Syncing Projects...")
				projects.forEach { projectDef ->
					syncProgressStatus(projectDef.name, ProjectsList.Status.Syncing)

					suspend fun onProgress(progress: Float, message: String?) {
						syncProgressStatus(projectDef.name, ProjectsList.Status.Syncing, progress)
						if (message != null) onSyncLog(message)
					}

					val projectSuccess = syncProject(projectDef, ::onSyncLog, ::onProgress)
					allSuccess = allSuccess && projectSuccess

					val newStatus = if (projectSuccess) ProjectsList.Status.Complete else ProjectsList.Status.Failed
					syncProgressStatus(projectDef.name, newStatus)

					yield()
				}
			} else {
				projects.forEach { projectDef ->
					syncProgressStatus(projectDef.name, ProjectsList.Status.Failed)
				}
			}

			callback(allSuccess)

			withContext(mainDispatcher) {
				_state.getAndUpdate {
					it.copy(
						syncState = it.syncState.copy(
							syncComplete = true
						)
					)
				}

				loadProjectList()

				if (allSuccess && globalSettingsRepository.globalSettings.autoCloseSyncDialog) {
					hideProjectsSync()
				}
			}
		}
	}

	override fun cancelProjectsSync() {
		syncProjectsJob?.cancel(CancellationException("User canceled sync"))
		syncProjectsJob = null

		scope.launch(mainDispatcher) {
			onSyncLog("Sync canceled by user")

			withContext(mainDispatcher) {
				_state.getAndUpdate {
					val statuses = it.syncState.projectsStatus.toMutableMap()
					it.syncState.projectsStatus.values.forEach { status ->
						if (status.status == ProjectsList.Status.Syncing || status.status == ProjectsList.Status.Pending) {
							statuses[status.projectName] = status.copy(
								status = ProjectsList.Status.Canceled
							)
						}
					}

					it.copy(
						syncState = it.syncState.copy(
							syncComplete = true,
							projectsStatus = statuses
						),
					)
				}
			}
		}
	}

	override fun hideProjectsSync() {
		resetSync()
	}

	override fun showProjectsSync() {
		_state.getAndUpdate {
			it.copy(
				syncState = it.syncState.copy(
					showProjectSync = true
				),
			)
		}
	}

	private suspend fun onSyncLog(message: String) {
		Napier.i(message)
		withContext(mainDispatcher) {
			_state.getAndUpdate {
				it.copy(
					syncState = it.syncState.copy(
						syncLog = it.syncState.syncLog + message
					),
				)
			}
		}
	}

	private fun resetSync() {
		_state.getAndUpdate {
			it.copy(
				syncState = ProjectsList.SyncState()
			)
		}
	}
}