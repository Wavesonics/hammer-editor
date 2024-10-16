package com.darkrockstudios.apps.hammer.common.data.globalsettings

import com.darkrockstudios.apps.hammer.common.data.globalsettings.datasource.GlobalSettingsDatasource
import com.darkrockstudios.apps.hammer.common.data.globalsettings.datasource.ServerSettingsDatasource
import com.darkrockstudios.apps.hammer.common.fileio.HPath
import com.darkrockstudios.apps.hammer.common.fileio.okio.toHPath
import com.darkrockstudios.apps.hammer.common.getDefaultRootDocumentDirectory
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent

class GlobalSettingsRepository(
	private val globalSettingsDatasource: GlobalSettingsDatasource,
	private val serverSettingsDatasource: ServerSettingsDatasource,
) : KoinComponent {

	private val lock = reentrantLock()

	var globalSettings: GlobalSettings
		private set
	private fun projectsDir(): HPath = globalSettings.projectsDirectory.toPath().toHPath()

	private val _globalSettingsUpdates = MutableSharedFlow<GlobalSettings>(
		extraBufferCapacity = 1,
		replay = 1,
		onBufferOverflow = BufferOverflow.DROP_OLDEST,
	)
	val globalSettingsUpdates: SharedFlow<GlobalSettings> = _globalSettingsUpdates

	private val _serverSettingsUpdates = MutableSharedFlow<ServerSettings?>(
		extraBufferCapacity = 1,
		replay = 1,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)
	val serverSettingsUpdates: SharedFlow<ServerSettings?> = _serverSettingsUpdates

	var serverSettings: ServerSettings?
		private set

	init {
		globalSettings = globalSettingsDatasource.loadSettings()
		_globalSettingsUpdates.tryEmit(globalSettings)

		serverSettings = serverSettingsDatasource.loadServerSettings(projectsDir())
		_serverSettingsUpdates.tryEmit(serverSettings)
	}

	suspend fun updateSettings(action: (GlobalSettings) -> GlobalSettings) {
		val settings = globalSettingsUpdates.first()
		lock.withLock {
			val updated = action(settings)
			dispatchSettingsUpdate(updated)

			if (settings.projectsDirectory != updated.projectsDirectory) {
				val serverSettings = serverSettingsDatasource.loadServerSettings(projectsDir())
				_serverSettingsUpdates.tryEmit(serverSettings)
			}
		}
	}

	private fun dispatchSettingsUpdate(settings: GlobalSettings) {
		globalSettingsDatasource.storeSettings(settings)
		globalSettings = settings
		_globalSettingsUpdates.tryEmit(settings)
	}

	fun updateServerSettings(settings: ServerSettings) {
		serverSettingsDatasource.storeServerSettings(settings, projectsDir())

		serverSettings = settings
		_serverSettingsUpdates.tryEmit(settings)
	}

	fun serverIsSetup(): Boolean = serverSettingsDatasource.serverIsSetup(projectsDir())

	fun defaultProjectDir() = GlobalSettingsRepository.defaultProjectDir().toHPath()

	fun deleteServerSettings() {
		serverSettingsDatasource.removeServerSettings(projectsDir())

		serverSettings = null
		_serverSettingsUpdates.tryEmit(null)
	}

	companion object {
		const val DEFAULT_PROJECTS_DIR = "HammerProjects"

		private fun defaultProjectDir() = getDefaultRootDocumentDirectory().toPath() / DEFAULT_PROJECTS_DIR

		fun createDefault(): GlobalSettings {
			return GlobalSettings(
				projectsDirectory = defaultProjectDir().toString()
			)
		}
	}
}