package com.darkrockstudios.apps.hammer.common.components.storyeditor.focusmode

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.darkrockstudios.apps.hammer.common.components.ProjectComponentBase
import com.darkrockstudios.apps.hammer.common.data.ProjectDef

class FocusModeComponent(
	componentContext: ComponentContext,
	projectDef: ProjectDef,
	private val closeFocusMode: () -> Unit
) : ProjectComponentBase(projectDef, componentContext), FocusMode {

	override val state = MutableValue(
		FocusMode.State(projectDef)
	)

	override fun dismiss() {
		closeFocusMode()
	}
}