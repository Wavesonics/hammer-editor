package com.darkrockstudios.apps.hammer.common.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.base.http.ApiProjectEntity
import com.darkrockstudios.apps.hammer.base.http.ApiSceneType
import com.darkrockstudios.apps.hammer.common.components.projectsync.ProjectSync
import com.darkrockstudios.apps.hammer.common.projectsync.ProjectSynchronizationContent
import com.darkrockstudios.apps.hammer.common.projectsync.RemoteEntry
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun expandedSize(): WindowSizeClass {
	return WindowSizeClass.calculateFromSize(
		size = Size.Zero.copy(1920f, 1280f),
		density = Density(1f)
	)
}

@Preview
@Composable
private fun SceneConflictPreview() {
	val serverScene = ApiProjectEntity.SceneEntity(
		id = 1,
		sceneType = ApiSceneType.Scene,
		order = 1,
		name = "Scene Name",
		path = listOf(3, 5),
		content = sceneContent,
		outline = "",
		notes = "",
	)
	val clientEntity = serverScene.copy(
		content = sceneContent.replace("to", "BOB")
	)

	val conflict = ProjectSync.EntityConflict.SceneConflict(
		serverScene = clientEntity,
		clientScene = serverScene
	)

	ProjectSynchronizationPreview(conflict, expandedSize())
}

@Preview
@Composable
private fun NoteConflictPreview() {
	val serverEntity = ApiProjectEntity.NoteEntity(
		id = 1,
		content = sceneContent,
		created = Clock.System.now()
	)
	val clientEntity = serverEntity.copy(
		content = sceneContent.replace("to", "BOB")
	)

	val conflict = ProjectSync.EntityConflict.NoteConflict(
		serverNote = clientEntity,
		clientNote = serverEntity
	)

	ProjectSynchronizationPreview(conflict, expandedSize())
}

@Preview
@Composable
private fun TimelineConflictPreview() {
	val serverEntity = ApiProjectEntity.TimelineEventEntity(
		id = 1,
		content = sceneContent,
		date = "October 1st",
		order = 1
	)
	val clientEntity = serverEntity.copy(
		content = sceneContent.replace("to", "BOB"),
		date = "November 2nd"
	)

	val conflict = ProjectSync.EntityConflict.TimelineEventConflict(
		serverEvent = clientEntity,
		clientEvent = serverEntity
	)

	ProjectSynchronizationPreview(conflict, expandedSize())
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun ProjectSynchronizationPreview(
	conflict: ProjectSync.EntityConflict<*>,
	screenCharacteristics: WindowSizeClass = calculateWindowSizeClass()
) {
	KoinApplicationPreview {
		ProjectSynchronizationContent(
			component = previewProjectSyncComponent(conflict),
			showSnackbar = {},
			screenCharacteristics = screenCharacteristics
		)
	}
}

private fun previewProjectSyncComponent(conflict: ProjectSync.EntityConflict<*>?): ProjectSync {
	val compoent = object : ProjectSync {
		override val state = MutableValue(ProjectSync.State())
		override fun syncProject(onComplete: (Boolean) -> Unit) {}
		override fun resolveConflict(resolvedEntity: ApiProjectEntity): ProjectSync.EntityMergeError? {
			return null
		}
		override fun endSync() {}
		override fun cancelSync() {}
		override fun showLog(show: Boolean) {}
	}

	compoent.state.update {
		ProjectSync.State(
			isSyncing = true,
			entityConflict = conflict,
			conflictTitle = MR.strings.sync_conflict_scene_title,
		)
	}

	return compoent
}

private val sceneContent = "Alice was beginning to get very tired of sitting by her sister\n" +
		"on the bank, and of having nothing to do:  once or twice she had\n" +
		"peeped into the book her sister was reading, but it had no\n" +
		"pictures or conversations in it, `and what is the use of a book,'\n" +
		"thought Alice `without pictures or conversation?'\n" +
		"\n" +
		"So she was considering in her own mind (as well as she could,\n" +
		"for the hot day made her feel very sleepy and stupid), whether\n" +
		"the pleasure of making a daisy-chain would be worth the trouble\n" +
		"of getting up and picking the daisies, when suddenly a White\n" +
		"Rabbit with pink eyes ran close by her.\n" +
		"\n" +
		"There was nothing so VERY remarkable in that; nor did Alice\n" +
		"think it so VERY much out of the way to hear the Rabbit say to\n" +
		"itself, `Oh dear!  Oh dear!  I shall be late!'  (when she thought\n" +
	"it over afterwards, it occurred to her that she ought to have\n" +
	"wondered at this, but at the time it all seemed quite natural);\n" +
	"but when the Rabbit actually TOOK A WATCH OUT OF ITS WAISTCOAT-\n" +
	"POCKET, and looked at it, and then hurried on, Alice started to\n" +
	"her feet, for it flashed across her mind that she had never\n" +
	"before seen a rabbit with either a waistcoat-pocket, or a watch to\n" +
	"take out of it, and burning with curiosity, she ran across the\n" +
	"field after it, and fortunately was just in time to see it pop\n" +
	"down a large rabbit-hole under the hedge.\n" +
	"\n"

@Preview
@Composable
private fun RemotePreview() {
	val entity = ApiProjectEntity.EncyclopediaEntryEntity(
		id = 1,
		name = "Test",
		entryType = "person",
		text = "Test, Test, Test, Test, Test,Test,v,v,v,v,v,",
		tags = setOf("tag1", "tag2"),
		image = null
	)

	RemoteEntry(
		entityConflict = ProjectSync.EntityConflict.EncyclopediaEntryConflict(entity, entity),
		component = object : ProjectSync {
			override val state = MutableValue(
				ProjectSync.State(
				)
			)

			override fun syncProject(onComplete: (Boolean) -> Unit) {}
			override fun resolveConflict(resolvedEntity: ApiProjectEntity): ProjectSync.EntityMergeError? {
				return null
			}
			override fun endSync() {}
			override fun cancelSync() {}
			override fun showLog(show: Boolean) {}
		}
	)
}