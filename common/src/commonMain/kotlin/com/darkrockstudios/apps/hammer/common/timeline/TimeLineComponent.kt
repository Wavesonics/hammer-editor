package com.darkrockstudios.apps.hammer.common.timeline

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.reduce
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.darkrockstudios.apps.hammer.common.ProjectComponentBase
import com.darkrockstudios.apps.hammer.common.data.MenuDescriptor
import com.darkrockstudios.apps.hammer.common.data.ProjectDef
import com.darkrockstudios.apps.hammer.common.data.id.IdRepository
import com.darkrockstudios.apps.hammer.common.data.timelinerepository.TimeLineContainer
import com.darkrockstudios.apps.hammer.common.data.timelinerepository.TimeLineEvent
import com.darkrockstudios.apps.hammer.common.data.timelinerepository.TimeLineRepository
import com.darkrockstudios.apps.hammer.common.dependencyinjection.injectMainDispatcher
import com.darkrockstudios.apps.hammer.common.projectInject
import com.darkrockstudios.apps.hammer.common.util.debounceUntilQuiescent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class TimeLineComponent(
	componentContext: ComponentContext,
	projectDef: ProjectDef,
	addMenu: (menu: MenuDescriptor) -> Unit,
	removeMenu: (id: String) -> Unit
) : ProjectComponentBase(projectDef, componentContext), TimeLine {

	private val mainDispatcher by injectMainDispatcher()
	private val idRepository: IdRepository by projectInject()
	private val timeLineRepository: TimeLineRepository by projectInject()

	private val _state = MutableValue(TimeLine.State(timeLine = null))
	override val state: Value<TimeLine.State> = _state

	private val timelineFlow = MutableSharedFlow<TimeLineContainer>(
		extraBufferCapacity = 1,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)

	private var saveJob: Job? = null

	init {
		lifecycle.doOnCreate {
			saveOnChange()
			watchTimeLine()
		}

		lifecycle.doOnDestroy {
			saveJob?.cancel()
			// Save final
			state.value.timeLine?.let { timeLine ->
				timeLineRepository.storeTimeline(timeLine)
			}
		}
	}

	private fun setTimeline(timeline: TimeLineContainer) {
		_state.reduce {
			it.copy(timeLine = timeline)
		}
		timelineFlow.tryEmit(timeline)
	}

	private fun watchTimeLine() {
		scope.launch {
			timeLineRepository.timelineFlow.collect { timeLine ->
				withContext(mainDispatcher) {
					if (timeLine != state.value.timeLine) {
						_state.reduce {
							it.copy(timeLine = timeLine)
						}
					}
				}
			}
		}
	}

	private fun saveOnChange() {
		saveJob = scope.launch {
			timelineFlow.debounceUntilQuiescent(1000.milliseconds).collect { timeLine ->
				Napier.d("comp store timeline")
				timeLineRepository.storeTimeline(timeLine)
			}
		}
	}

	override fun createEvent(dateText: String?, contentText: String): Boolean {
		val timeline = _state.value.timeLine ?: return false

		val id = idRepository.claimNextId()

		val date = if (dateText?.isNotBlank() == true) {
			dateText
		} else {
			null
		}
		val event = TimeLineEvent(
			id = id,
			date = date,
			content = contentText
		)

		val newEvents = timeline.events.toMutableList()
		newEvents.add(event)

		val updatedTimeline = timeline.copy(
			events = newEvents
		)

		timeLineRepository.storeTimeline(updatedTimeline)

		Napier.i { "Time Line event created! ${event.id}" }

		return true
	}

	override fun updateEvent(event: TimeLineEvent) {
		val timeline = _state.value.timeLine ?: return

		val events = timeline.events.toMutableList()
		val index = events.indexOfFirst { it.id == event.id }
		events[index] = event

		val updatedTimeline = timeline.copy(
			events = events
		)

		setTimeline(updatedTimeline)
	}

	override fun moveEvent(event: TimeLineEvent, toIndex: Int, after: Boolean): Boolean {
		val timeline = _state.value.timeLine ?: return false
		val events = timeline.events.toMutableList()
		val fromIndex = events.indexOfFirst { it.id == event.id }

		return if (fromIndex <= -1) {
			Napier.e { "moveEvent from event not found!" }
			false
		} else if (toIndex >= events.size) {
			Napier.e { "moveEvent to invalid index: $toIndex" }
			false
		} else {
			// Good to go
			val computedToIndex = if (after) {
				toIndex + 1
			} else {
				toIndex
			}

			val moved = if (computedToIndex < fromIndex) {
				events.removeAt(fromIndex)
				events.add(computedToIndex, event)
				true
			} else if (computedToIndex > fromIndex) {
				events.add(computedToIndex, event)
				events.removeAt(fromIndex)
				true
			} else {
				Napier.d { "Can't move" }
				false
			}

			if (moved) {
				val updatedTimeline = timeline.copy(
					events = events
				)

				setTimeline(updatedTimeline)
			}

			moved
		}
	}
}