package com.darkrockstudios.apps.hammer.common.projects

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState

@Composable
fun ProjectsUi(component: Projects, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    var projectDirText by remember { mutableStateOf(state.projectsDir) }

    Column(modifier = modifier) {
        Text(text = "Projects Directory")

        TextField(
            value = projectDirText,
            onValueChange = {
                projectDirText = it
                state.projectsDir = it
            },
            label = { Text("Label") }
        )

        Button(onClick = { component.loadProjectList(projectDirText) }) {
            Text("Load")
        }
    }
}