package com.darkrockstudios.apps.hammer.base.http

import kotlinx.datetime.Instant
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
sealed interface ApiProjectEntity {
	val type: Type
	val id: Int

	@Serializable
	data class SceneEntity(
		override val type: Type = Type.SCENE,
		override val id: Int,
		val sceneType: ApiSceneType,
		val order: Int,
		val name: String,
		val path: List<Int>,
		val content: String = "",
		val outline: String = "",
		val notes: String = "",
	) : ApiProjectEntity

	@Serializable
	data class NoteEntity(
		override val type: Type = Type.NOTE,
		override val id: Int,
		val content: String,
		val created: Instant
	) : ApiProjectEntity

	@Serializable
	data class TimelineEventEntity(
		override val type: Type = Type.TIMELINE_EVENT,
		override val id: Int,
		val order: Int,
		val date: String?,
		val content: String
	) : ApiProjectEntity

	@Serializable
	data class EncyclopediaEntryEntity(
		override val type: Type = Type.ENCYCLOPEDIA_ENTRY,
		override val id: Int,
		val name: String,
		val entryType: String,
		val text: String,
		val tags: Set<String>,
		val image: Image?,
	) : ApiProjectEntity {
		@Serializable
		data class Image(
			val base64: String,
			val fileExtension: String,
		)
	}

	@Serializable
	data class SceneDraftEntity(
		override val type: Type = Type.SCENE_DRAFT,
		override val id: Int,
		val sceneId: Int,
		val created: Instant,
		val name: String,
		val content: String
	) : ApiProjectEntity

	enum class Type(val id: Int) {
		SCENE(0),
		NOTE(1),
		TIMELINE_EVENT(2),
		ENCYCLOPEDIA_ENTRY(3),
		SCENE_DRAFT(4);

		fun toStringId() = when (this) {
			SCENE -> "scene"
			NOTE -> "note"
			TIMELINE_EVENT -> "timeline_event"
			ENCYCLOPEDIA_ENTRY -> "encyclopedia_entry"
			SCENE_DRAFT -> "scene_draft"
		}

		companion object {
			fun fromString(string: String?): Type? {
				return when (string?.trim()?.lowercase()) {
					"scene" -> SCENE
					"note" -> NOTE
					"timeline_event" -> TIMELINE_EVENT
					"encyclopedia_entry" -> ENCYCLOPEDIA_ENTRY
					"scene_draft" -> SCENE_DRAFT
					else -> null
				}
			}

			fun fromInt(id: Int?): Type? {
				return entries.find { it.id == id }
			}
		}
	}
}