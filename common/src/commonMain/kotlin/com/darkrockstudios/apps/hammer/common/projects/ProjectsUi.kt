package com.darkrockstudios.apps.hammer.common.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState

@Composable
fun ProjectsUi(component: Projects, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    var projectDirText by remember { mutableStateOf(state.projectsDir) }

    Column(modifier = modifier) {
        Text(
            text = "Projects Directory",
            style = MaterialTheme.typography.h4
        )

        TextField(
            value = projectDirText,
            onValueChange = {
                projectDirText = it
                component.setProjectsDir(it)
            },
            label = { Text("Label") }
        )

        Button(onClick = { component.loadProjectList() }) {
            Text("Load")
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "\uD83D\uDCDD Projects:",
                        style = MaterialTheme.typography.h5
                    )
                }
            }
            items(state.projects.size) { index ->
                ProjectCard(state.projects[index])
            }
        }
    }
}

@Composable
fun ProjectCard(project: String) {
    Text(
        "Project: $project",
        style = MaterialTheme.typography.body1
    )
}