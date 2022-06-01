package com.darkrockstudios.apps.hammer.common.projecteditor.scenelist

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.darkrockstudios.apps.hammer.common.data.Project

class SceneListComponent(
    componentContext: ComponentContext,
    project: Project
) : SceneList, ComponentContext by componentContext {

    private val _value = MutableValue(State(project = project))
    override val state: Value<State> = _value

    data class State(
        val project: Project
    )

    override fun onSceneSelected() {

    }
}