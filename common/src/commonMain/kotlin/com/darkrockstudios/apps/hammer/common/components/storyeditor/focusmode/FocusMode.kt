package com.darkrockstudios.apps.hammer.common.components.storyeditor.focusmode

import com.arkivanov.decompose.value.Value
import com.darkrockstudios.apps.hammer.common.data.ProjectDef

interface FocusMode {
	val state: Value<State>

	data class State(
		val projectDef: ProjectDef,
	)

	fun dismiss()
}