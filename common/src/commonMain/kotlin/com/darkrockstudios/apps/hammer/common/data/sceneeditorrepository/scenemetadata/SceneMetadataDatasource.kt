package com.darkrockstudios.apps.hammer.common.data.sceneeditorrepository.scenemetadata

import com.darkrockstudios.apps.hammer.common.data.ProjectDef
import com.darkrockstudios.apps.hammer.common.fileio.HPath

interface SceneMetadataDatasource {
	val projectDef: ProjectDef

	suspend fun loadMetadata(sceneId: Int): SceneMetadata?
	suspend fun storeMetadata(metadata: SceneMetadata, sceneId: Int)

	fun getMetadataDirectory(): HPath
	fun getMetadataPath(id: Int): HPath
	fun reIdSceneMetadata(oldId: Int, newId: Int)

	companion object {
		const val DIRECTORY = ".metadata"
	}
}