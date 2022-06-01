package com.darkrockstudios.apps.hammer.common.projects

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.reduce
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.getProjectsForDirectory
import io.github.aakira.napier.Napier

class Projects(
    componentContext: ComponentContext,
    projectsDir: String,
    private val onProjectSelected: (project: Project) -> Unit
) {
    private val _value = MutableValue(State(projectsDir = projectsDir))
    val state: Value<State> = _value

    fun loadProjectList() {
        _value.reduce {
            Napier.v("Load projects in: ${state.value.projectsDir}")
            val projects = getProjectsForDirectory(state.value.projectsDir)
            Napier.v("Projects: ${projects.joinToString()}")
            _value.value.copy(projects = projects)
        }
    }

    fun setProjectsDir(path: String) {
        _value.reduce {
            _value.value.copy(projectsDir = path)
        }
    }

    fun selectProject(project: Project) = onProjectSelected(project)

    data class State(
        val projectsDir: String = "",
        val projects: List<Project> = mutableListOf()
    )
}