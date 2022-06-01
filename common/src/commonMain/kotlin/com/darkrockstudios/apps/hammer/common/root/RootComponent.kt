package com.darkrockstudios.apps.hammer.common.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.router
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.projects.Projects

class RootComponent(
    componentContext: ComponentContext,
    private val onProjectSelected: (project: Project) -> Unit
) : Root, ComponentContext by componentContext {

    private val router: Router<Config, Root.Screen> =
        router(
            initialConfiguration = Config.Projects(),
            childFactory = ::createChild
        )

    override val routerState: Value<RouterState<*, Root.Screen>> = router.state

    private fun createChild(config: Config, componentContext: ComponentContext): Root.Screen =
        when (config) {
            is Config.Projects -> Root.Screen.ProjectsScreen(Projects(componentContext, config.projectsDir, onProjectSelected))
        }

    private sealed class Config : Parcelable {
        @Parcelize
        class Projects(val projectsDir: String = "") : Config()
    }
}