// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.darkrockstudios.apps.hammer.common.projects.ProjectSelection
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.project.ProjectEditor
import com.darkrockstudios.apps.hammer.common.root.RootComponent
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun main() {
    Napier.base(DebugAntilog())

    val lifecycle = LifecycleRegistry()
    application {
        val applicationState = remember { ApplicationState() }

        val root = RootComponent(
            componentContext = DefaultComponentContext(lifecycle),
            onProjectSelected = { project ->
                applicationState.openProject(project)
            }
        )

        when (applicationState.windows.value) {
            is WindowState.ProjectSectionWindow -> {
                ProjectSelectionWindow(root)
            }
            is WindowState.ProjectWindow -> {
                ProjectWindow(root, applicationState)
            }
        }
    }
}

@Composable
private fun ApplicationScope.ProjectSelectionWindow(root: RootComponent) {
    val windowState = rememberWindowState()
    Window(
        title = "Project Selection",
        state = windowState,
        onCloseRequest = ::exitApplication,
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme {
                ProjectSelection(root)
            }
        }
    }
}

@Composable
private fun ApplicationScope.ProjectWindow(root: RootComponent, app: ApplicationState) {
    val windowState = rememberWindowState()
    Window(
        title = "Hammer",
        state = windowState,
        onCloseRequest = { app.closeProject() },
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme {
                ProjectEditor()
            }
        }
    }
}


private class ApplicationState {
    val windows = mutableStateOf<WindowState>(WindowState.ProjectSectionWindow())

    fun openProject(project: Project) {
        windows.value = WindowState.ProjectWindow(project)
    }

    fun closeProject() {
        windows.value = WindowState.ProjectSectionWindow()
    }
}

private sealed class WindowState {
    data class ProjectSectionWindow(private val _data: Boolean = true) : WindowState()

    data class ProjectWindow(val project: Project) : WindowState()
}