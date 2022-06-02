package com.darkrockstudios.apps.hammer.common.projecteditor.sceneeditor

import com.arkivanov.decompose.value.Value

interface SceneEditor {
    val state: Value<SceneEditorComponent.State>

    fun addEditorMenu()
    fun removeEditorMenu()
}