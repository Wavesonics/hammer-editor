// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.darkrockstudios.apps.hammer.common.projectselection.ProjectSelectionComponent
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.projecteditor.ProjectEditorComponent
import com.darkrockstudios.apps.hammer.common.projecteditor.ProjectEditorUi
import com.darkrockstudios.apps.hammer.common.projectselection.ProjectSelectionUi
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun main() {
    Napier.base(DebugAntilog())

    application {
        val applicationState = remember { ApplicationState() }
        val compContext = DefaultComponentContext(LifecycleRegistry())

        when (val windowState = applicationState.windows.value) {
            is WindowState.ProjectSectionWindow -> {
                ProjectSelectionWindow(compContext) { project ->
                    applicationState.openProject(project)
                }
            }
            is WindowState.ProjectWindow -> {
                ProjectEditorWindow(compContext, applicationState, windowState.project)
            }
        }
    }
}

@Composable
private fun ApplicationScope.ProjectSelectionWindow(
    compContext: DefaultComponentContext,
    onProjectSelected: (project: Project) -> Unit
) {
    val windowState = rememberWindowState()
    Window(
        title = "Project Selection",
        state = windowState,
        onCloseRequest = ::exitApplication,
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme {
                ProjectSelectionUi(ProjectSelectionComponent(compContext, onProjectSelected))
            }
        }
    }
}

@Composable
private fun ApplicationScope.ProjectEditorWindow(
    compContext: DefaultComponentContext,
    app: ApplicationState,
    project: Project
) {
    val windowState = rememberWindowState()
    Window(
        title = "Hammer",
        state = windowState,
        onCloseRequest = { app.closeProject() },
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme {
                ProjectEditorUi(ProjectEditorComponent(compContext, project))
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