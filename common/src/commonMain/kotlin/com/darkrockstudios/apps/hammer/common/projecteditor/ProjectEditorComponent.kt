package com.darkrockstudios.apps.hammer.common.projecteditor

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.decompose.value.reduce
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.data.Scene
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class ProjectEditorComponent(
    componentContext: ComponentContext,
    project: Project,
) : ProjectEditorRoot, ComponentContext by componentContext {

    //private val isDetailsToolbarVisible = BehaviorSubject(!_models.value.isMultiPane)
    private val selectedSceneFlow = MutableSharedFlow<Scene?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val listRouter =
        ListRouter(
            componentContext = this,
            project = project,
            selectedScene = selectedSceneFlow,
            onSceneSelected = ::onSceneSelected
        )

    override val listRouterState: Value<RouterState<*, ProjectEditorRoot.Child.List>> = listRouter.state

    private val detailsRouter =
        DetailsRouter(
            componentContext = this,
            onFinished = {}
        )

    override val detailsRouterState: Value<RouterState<*, ProjectEditorRoot.Child.Detail>> = detailsRouter.state

    private val _state = MutableValue(ProjectEditorRoot.State(project))
    override val state: Value<ProjectEditorRoot.State> = _state

    init {
        backPressedHandler.register {
            if (isMultiPaneMode() || !detailsRouter.isShown()) {
                false
            } else {
                closeDetailsAndShowList()
                true
            }
        }

        detailsRouter.state.observe(lifecycle) {
            (it.activeChild.configuration as? DetailsRouter.Config.SceneEditor)?.let { sceneEditor ->
                selectedSceneFlow.tryEmit(sceneEditor.scene)
            }
        }
    }

    private fun closeDetailsAndShowList() {
        listRouter.show()
        detailsRouter.closeScene()
    }

    private fun onSceneSelected(scene: Scene) {
        detailsRouter.showScene(scene)

        if (isMultiPaneMode()) {
            listRouter.show()
        } else {
            listRouter.moveToBackStack()
        }
    }

    override fun setMultiPane(isMultiPane: Boolean) {
        _state.reduce { it.copy(isMultiPane = isMultiPane) }
        //isDetailsToolbarVisible.onNext(!isMultiPane)

        if (isMultiPane) {
            switchToMultiPane()
        } else {
            switchToSinglePane()
        }
    }

    private fun switchToMultiPane() {
        listRouter.show()
    }

    private fun switchToSinglePane() {
        if (detailsRouter.isShown()) {
            listRouter.moveToBackStack()
        } else {
            listRouter.show()
        }
    }

    private fun isMultiPaneMode(): Boolean = _state.value.isMultiPane
}