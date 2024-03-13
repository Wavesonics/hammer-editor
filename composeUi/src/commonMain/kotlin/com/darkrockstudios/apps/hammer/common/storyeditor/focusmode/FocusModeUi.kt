package com.darkrockstudios.apps.hammer.common.storyeditor.focusmode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.common.TextEditorDefaults
import com.darkrockstudios.apps.hammer.common.components.storyeditor.focusmode.FocusMode
import com.darkrockstudios.apps.hammer.common.compose.ComposeRichText
import com.darkrockstudios.apps.hammer.common.compose.Ui
import com.darkrockstudios.apps.hammer.common.compose.moko.get
import com.darkrockstudios.apps.hammer.common.storyeditor.sceneeditor.EditorToolBar
import com.darkrockstudios.apps.hammer.common.storyeditor.sceneeditor.getInitialEditorContent
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeUi(component: FocusMode) {
	val state by component.state.subscribeAsState()
	val lastForceUpdate by component.lastForceUpdate.subscribeAsState()

	var textState by remember {
		mutableStateOf(
			getInitialEditorContent(state.sceneBuffer?.content)
		)
	}

	LaunchedEffect(lastForceUpdate) {
		textState = getInitialEditorContent(state.sceneBuffer?.content)
	}

	LaunchedEffect(textState.annotatedString) {
		component.onContentChanged(ComposeRichText(textState))
	}

	Column(modifier = Modifier.fillMaxSize()) {
		Row(
			modifier = Modifier
				.padding(
					start = Ui.Padding.L,
					end = Ui.Padding.L,
				)
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			Text(
				state.sceneItem.name,
				style = MaterialTheme.typography.headlineLarge,
				color = MaterialTheme.colorScheme.onBackground,
			)

			IconButton(onClick = component::dismiss) {
				Icon(
					imageVector = Icons.Default.Close,
					contentDescription = MR.strings.scene_editor_menu_item_close.get(),
					tint = MaterialTheme.colorScheme.onBackground
				)
			}
		}

		EditorToolBar(
			state = textState,
			decreaseTextSize = component::decreaseTextSize,
			increaseTextSize = component::increaseTextSize,
			resetTextSize = component::resetTextSize,
		)

		Row(
			modifier = Modifier.fillMaxSize(),
			horizontalArrangement = Arrangement.Center
		) {
			com.mohamedrejeb.richeditor.ui.material3.RichTextEditor(
				modifier = Modifier
					.background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
					.fillMaxHeight()
					.widthIn(128.dp, TextEditorDefaults.MAX_WIDTH)
					.padding(horizontal = Ui.Padding.XL),
				state = textState,
				shape = RectangleShape,
				colors = RichTextEditorDefaults.richTextEditorColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
				),
				placeholder = {
					androidx.compose.material.Text(
						MR.strings.scene_editor_body_placeholder.get(),
						color = MaterialTheme.colorScheme.onSurface,
					)
				},
				textStyle = MaterialTheme.typography.bodyLarge.copy(
					fontSize = state.textSize.sp,
					color = MaterialTheme.colorScheme.onSurface,
				),
			)
		}
	}
}