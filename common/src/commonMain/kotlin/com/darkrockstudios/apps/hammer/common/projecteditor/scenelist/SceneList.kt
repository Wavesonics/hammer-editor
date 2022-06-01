package com.darkrockstudios.apps.hammer.common.projecteditor.scenelist

import com.arkivanov.decompose.value.Value

interface SceneList {
    val state: Value<SceneListComponent.State>
    fun onSceneSelected()
}