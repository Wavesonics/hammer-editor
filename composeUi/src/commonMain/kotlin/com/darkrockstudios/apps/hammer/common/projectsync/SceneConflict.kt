package com.darkrockstudios.apps.hammer.common.projectsync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.base.http.ApiProjectEntity
import com.darkrockstudios.apps.hammer.common.components.projectsync.ProjectSync
import com.darkrockstudios.apps.hammer.common.compose.Ui
import com.darkrockstudios.apps.hammer.common.compose.moko.get

@Composable
internal fun SceneConflict(
	entityConflict: ProjectSync.EntityConflict.SceneConflict,
	component: ProjectSync,
	screenCharacteristics: WindowSizeClass
) {
	EntityConflict(
		entityConflict = entityConflict,
		component = component,
		screenCharacteristics = screenCharacteristics,
		LocalEntity = { m, c, p -> LocalScene(m, c, p) },
		RemoteEntity = { m, c, p -> RemoteScene(m, c, p) },
	)
}

@Composable
private fun LocalScene(
	modifier: Modifier = Modifier,
	entityConflict: ProjectSync.EntityConflict<ApiProjectEntity.SceneEntity>,
	component: ProjectSync
) {
	val entity = component.state.value.entityConflict?.clientEntity as? ApiProjectEntity.SceneEntity
	var nameTextValue by remember(entity) { mutableStateOf(entity?.name ?: "") }
	var contentTextValue by remember(entity) { mutableStateOf(entity?.content ?: "") }

	Column(modifier = modifier.padding(Ui.Padding.M)) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = MR.strings.sync_conflict_title_scene_local.get(),
				style = MaterialTheme.typography.headlineSmall
			)
			Button(onClick = {
				component.resolveConflict(
					entityConflict.clientEntity.copy(
						name = nameTextValue,
						content = contentTextValue
					)
				)
			}) {
				Text(MR.strings.sync_conflict_local_use_button.get())
			}
		}
		Text(
			text = MR.strings.sync_conflict_merge_explained.get(),
			style = MaterialTheme.typography.bodySmall,
			fontStyle = FontStyle.Italic
		)
		Spacer(Modifier.size(Ui.Padding.XL))
		TextField(
			value = nameTextValue,
			onValueChange = { nameTextValue = it },
			placeholder = { Text(MR.strings.sync_conflict_title_scene_field_name.get()) },
			label = { Text(MR.strings.sync_conflict_title_scene_field_name.get()) }
		)
		Spacer(Modifier.size(Ui.Padding.XL))
		TextField(
			value = contentTextValue,
			onValueChange = { contentTextValue = it },
			placeholder = { Text(MR.strings.sync_conflict_title_scene_field_content.get()) },
			label = { Text(MR.strings.sync_conflict_title_scene_field_content.get()) }
		)
	}
}

@Composable
private fun RemoteScene(
	modifier: Modifier = Modifier,
	entityConflict: ProjectSync.EntityConflict<ApiProjectEntity.SceneEntity>,
	component: ProjectSync
) {
	Column(modifier = modifier.padding(Ui.Padding.L)) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = MR.strings.sync_conflict_title_scene_remote.get(),
				style = MaterialTheme.typography.headlineSmall
			)
			Button(onClick = { component.resolveConflict(entityConflict.serverEntity) }) {
				Text(MR.strings.sync_conflict_remote_use_button.get())
			}
		}
		Text(
			MR.strings.sync_conflict_title_scene_field_name.get(),
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.Bold
		)
		SelectionContainer {
			Text(
				entityConflict.serverEntity.name,
				style = MaterialTheme.typography.bodyLarge
			)
		}
		Spacer(Modifier.size(Ui.Padding.XL))
		Text(
			MR.strings.sync_conflict_title_scene_field_content.get(),
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.Bold
		)
		SelectionContainer {
			Text(
				entityConflict.serverEntity.content,
				modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
			)
		}
	}
}