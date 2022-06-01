package com.darkrockstudios.apps.hammer.common.projecteditor

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.router
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.data.Scene
import com.darkrockstudios.apps.hammer.common.projecteditor.sceneeditor.SceneEditorComponent
import com.darkrockstudios.apps.hammer.common.projecteditor.scenelist.SceneListComponent

class ProjectEditorComponent(
    componentContext: ComponentContext,
    project: Project,
): ProjectEditorRoot, ComponentContext by componentContext {

    private val router: Router<Config, ProjectEditorRoot.Child> =
        router(
            initialConfiguration = Config.SceneListConfig(project),
            childFactory = ::createChild
        )

    override val routerState: Value<RouterState<*, ProjectEditorRoot.Child>> = router.state

    private fun createChild(config: Config, componentContext: ComponentContext): ProjectEditorRoot.Child =
        when (config) {
            is Config.SceneListConfig -> ProjectEditorRoot.Child.List(
                SceneListComponent(componentContext, config.project)
            )
            is Config.SceneEditorConfig -> ProjectEditorRoot.Child.Editor(
                SceneEditorComponent(componentContext, config.scene)
            )
        }

    private sealed class Config : Parcelable {
        @Parcelize
        class SceneListConfig(val project: Project) : Config()

        @Parcelize
        class SceneEditorConfig(val scene: Scene) : Config()
    }
}