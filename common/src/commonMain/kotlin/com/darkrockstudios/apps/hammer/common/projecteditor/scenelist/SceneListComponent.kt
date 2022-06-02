package com.darkrockstudios.apps.hammer.common.projecteditor.scenelist

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.reduce
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.data.Scene
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

class SceneListComponent(
    componentContext: ComponentContext,
    project: Project,
    selectedScene: SharedFlow<Scene?>,
    private val sceneSelected: (scene: Scene) -> Unit
) : SceneList, ComponentContext by componentContext {

    private val _state = MutableValue(SceneList.State(project = project))
    override val state: Value<SceneList.State> = _state

    override fun onSceneSelected(scene: Scene) = sceneSelected(scene)

    init {
        selectedScene.onEach { scene ->
            _state.reduce { it.copy(selectedScene = scene) }
        }
    }
}