package com.darkrockstudios.apps.hammer.common.components.timeline

import com.arkivanov.decompose.value.Value
import com.darkrockstudios.apps.hammer.common.data.timelinerepository.TimeLineContainer
import com.darkrockstudios.apps.hammer.common.data.timelinerepository.TimeLineEvent

interface TimeLineOverview {
	val state: Value<State>

	suspend fun moveEvent(event: TimeLineEvent, toIndex: Int, after: Boolean): Boolean

	data class State(
		val timeLine: TimeLineContainer?
	)
}