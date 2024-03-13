package com.darkrockstudios.apps.hammer.common.storyeditor.sceneeditor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.common.TextEditorDefaults
import com.darkrockstudios.apps.hammer.common.components.storyeditor.sceneeditor.SceneEditor
import com.darkrockstudios.apps.hammer.common.compose.ComposeRichText
import com.darkrockstudios.apps.hammer.common.compose.RootSnackbarHostState
import com.darkrockstudios.apps.hammer.common.compose.Toaster
import com.darkrockstudios.apps.hammer.common.compose.Ui
import com.darkrockstudios.apps.hammer.common.compose.moko.get
import com.darkrockstudios.apps.hammer.common.storyeditor.scenelist.SceneDeleteDialog
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SceneEditorUi(
	component: SceneEditor,
	rootSnackbar: RootSnackbarHostState,
	modifier: Modifier = Modifier,
) {
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

	Toaster(component, rootSnackbar)

	BoxWithConstraints(modifier = modifier) {
		val boxWithConstraintsScope = this

		Column(modifier = Modifier.fillMaxHeight()) {
			EditorTopBar(component, rootSnackbar)

			EditorToolBar(
				state = textState,
				decreaseTextSize = component::decreaseTextSize,
				increaseTextSize = component::increaseTextSize,
				resetTextSize = component::resetTextSize,
			)

			//val verticalScrollState = rememberScrollState(0)
			Row(
				modifier = Modifier.fillMaxSize(),
				horizontalArrangement = Arrangement.Center
			) {
				com.mohamedrejeb.richeditor.ui.material3.RichTextEditor(
					modifier = Modifier
						.background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
						.fillMaxHeight()
						.widthIn(TextEditorDefaults.MAX_WIDTH, TextEditorDefaults.MAX_WIDTH)
						.defaultMinSize(minWidth = TextEditorDefaults.MAX_WIDTH)
						.padding(horizontal = Ui.Padding.XL),
					shape = RectangleShape,
					colors = RichTextEditorDefaults.richTextEditorColors(
						containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
					),
					state = textState,
					placeholder = {
						Text(
							MR.strings.scene_editor_body_placeholder.get(),
							color = MaterialTheme.colorScheme.onSurface,
							style = MaterialTheme.typography.bodyLarge.copy(
								fontSize = state.textSize.sp,
							)
						)
					},
					textStyle = MaterialTheme.typography.bodyLarge.copy(
						fontSize = state.textSize.sp,
						color = MaterialTheme.colorScheme.onSurface,
					),
				)

				Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

				/*
				MpScrollBar(
					modifier = Modifier.fillMaxHeight(),
					state = verticalScrollState
				)
				*/
				val remainingWidth = remember(boxWithConstraintsScope.maxWidth) {
					boxWithConstraintsScope.maxWidth - TextEditorDefaults.MAX_WIDTH
				}
				SceneMetadataSidebar(component, remainingWidth)
			}
		}
	}

	SaveDraftDialog(state, component) { message ->
		rootSnackbar.showSnackbar(message)
	}

	if (state.confirmDelete) {
		SceneDeleteDialog(state.sceneItem) { doDelete ->
			if (doDelete) {
				component.doDelete()
			} else {
				component.endDelete()
			}
		}
	}
}

@Composable
private fun SceneMetadataSidebar(component: SceneEditor, remainingWidth: Dp) {
	val state by component.state.subscribeAsState()

	if (remainingWidth >= SCENE_METADATA_MIN_WIDTH) {
		AnimatedVisibility(
			visible = state.showMetadata,
			enter = slideInHorizontally { it } + fadeIn(),
			exit = slideOutHorizontally { it } + fadeOut(),
		) {
			Box(modifier = Modifier.padding(Ui.Padding.L)) {
				SceneMetadataPanelUi(
					component = component.sceneMetadataComponent,
					modifier = Modifier.wrapContentWidth().widthIn(max = SCENE_METADATA_MAX_WIDTH).fillMaxHeight(),
					closeMetadata = component::toggleMetadataVisibility,
				)
			}
		}
	} else {
		if (state.showMetadata) {
			Dialog(onDismissRequest = component::toggleMetadataVisibility) {
				Box(modifier = Modifier.padding(Ui.Padding.L)) {
					SceneMetadataPanelUi(
						component = component.sceneMetadataComponent,
						modifier = Modifier.fillMaxWidth().wrapContentHeight(),
						closeMetadata = component::toggleMetadataVisibility,
					)
				}
			}
		}
	}
}