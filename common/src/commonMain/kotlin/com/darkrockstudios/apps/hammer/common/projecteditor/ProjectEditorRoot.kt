package com.darkrockstudios.apps.hammer.common.projecteditor

import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.Value
import com.darkrockstudios.apps.hammer.common.projecteditor.sceneeditor.SceneEditor
import com.darkrockstudios.apps.hammer.common.projecteditor.scenelist.SceneList

interface ProjectEditorRoot {
    val routerState: Value<RouterState<*, Child>>

    sealed class Child {
        class List(val component: SceneList) : Child()
        class Editor(val component: SceneEditor) : Child()
    }
}