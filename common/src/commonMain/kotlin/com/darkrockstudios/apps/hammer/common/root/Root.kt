package com.darkrockstudios.apps.hammer.common.root

import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.Value
import com.darkrockstudios.apps.hammer.common.projects.Projects

interface Root {

    val routerState: Value<RouterState<*, Screen>>

    sealed class Screen {
        class ProjectsScreen(val component: Projects) : Screen()
    }
}