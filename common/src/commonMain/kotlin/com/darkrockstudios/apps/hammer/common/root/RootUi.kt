package com.darkrockstudios.apps.hammer.common.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.darkrockstudios.apps.hammer.common.projects.ProjectsUi
import io.github.aakira.napier.Napier

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootUi(root: Root, modifier: Modifier = Modifier) {
    val routerState by root.routerState.subscribeAsState()
    val activeComponent = routerState.activeChild.instance

    Column(modifier = modifier) {
        Children(
            routerState = routerState,
            modifier = Modifier.weight(weight = 1F),
        ) {
            when (val child = it.instance) {
                is Root.Screen.ProjectsScreen -> ProjectsUi(
                    component = child.component,
                    modifier = Modifier.fillMaxSize(),
                    onProjectClick = { project ->
                        child.component.selectProject(project)
                        Napier.d { "Project Selected: ${project.name}" }
                    }
                )
            }
        }
    }
}