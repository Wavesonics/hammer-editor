package com.darkrockstudios.apps.hammer.common.projectselection

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.reduce
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.getProjectsForDirectory

class ProjectSelectionComponent(
    componentContext: ComponentContext,
    private val onProjectSelected: (project: Project) -> Unit
): ComponentContext by componentContext {

    private val _value = MutableValue(State(projectsDir = ""))
    val state: Value<State> = _value

    fun loadProjectList() {
        _value.reduce {
            val projects = getProjectsForDirectory(state.value.projectsDir)
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