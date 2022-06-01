package com.darkrockstudios.apps.hammer.common.projecteditor.sceneeditor

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.darkrockstudios.apps.hammer.common.data.Scene


class SceneEditorComponent(
    componentContext: ComponentContext,
    scene: Scene
): SceneEditor, ComponentContext by componentContext {

    private val _value = MutableValue(State(scene = scene))
    override val state: Value<State> = _value

    data class State(
        val scene: Scene
    )
}