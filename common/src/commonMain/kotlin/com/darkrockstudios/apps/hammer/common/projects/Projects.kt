package com.darkrockstudios.apps.hammer.common.projects

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.aakira.napier.Napier

class Projects(
    componentContext: ComponentContext,
    projectsDir: String
) {
    private val _value = MutableValue(State(projectsDir = projectsDir))
    val state: Value<State> = _value

    fun loadProjectList(projectsDir: String) {
        Napier.v("Load projects in: $projectsDir")
        /*
        _value.reduce {

        }
        */
    }

    data class State(
        var projectsDir: String = "",
        val projects: MutableList<String> = mutableListOf()
    )
}