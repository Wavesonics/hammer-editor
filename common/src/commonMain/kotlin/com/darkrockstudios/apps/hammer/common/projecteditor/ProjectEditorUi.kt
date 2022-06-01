package com.darkrockstudios.apps.hammer.common.projecteditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.darkrockstudios.apps.hammer.common.projecteditor.sceneeditor.SceneEditorUi
import com.darkrockstudios.apps.hammer.common.projecteditor.scenelist.SceneListUi

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun ProjectEditorUi(
    projectEditorComponent: ProjectEditorComponent,
    modifier: Modifier = Modifier
) {
    val routerState by projectEditorComponent.routerState.subscribeAsState()
    val activeComponent = routerState.activeChild.instance

    Column(modifier = modifier) {
        Children(
            routerState = routerState,
            modifier = Modifier.weight(weight = 1F),
        ) {
            when (val child = it.instance) {
                is ProjectEditorRoot.Child.List -> SceneListUi(
                    component = child.component,
                    modifier = Modifier.fillMaxSize()
                )
                is ProjectEditorRoot.Child.Editor -> SceneEditorUi(
                    component = child.component,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}