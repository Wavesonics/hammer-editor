package com.darkrockstudios.apps.hammer.common.storyeditor.sceneeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.darkrockstudios.apps.hammer.MR
import com.mohamedrejeb.richeditor.model.RichTextState

@Composable
fun EditorToolBar(
	state: RichTextState,
	decreaseTextSize: () -> Unit,
	increaseTextSize: () -> Unit,
	resetTextSize: () -> Unit,
) {
	val currentSpanStyle = state.currentSpanStyle

	Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
		EditorAction(
			iconRes = MR.images.icon_bold,
			active = currentSpanStyle.fontWeight == FontWeight.Bold,
		) {
			state.toggleSpanStyle(
				SpanStyle(
					fontWeight = FontWeight.Bold
				)
			)
		}
		EditorAction(
			iconRes = MR.images.icon_italic,
			active = currentSpanStyle.fontStyle == FontStyle.Italic,
		) {
			state.toggleSpanStyle(
				SpanStyle(
					fontStyle = FontStyle.Italic
				)
			)
		}
//		EditorAction(
//			iconRes = MR.images.icon_undo,
//			active = sceneTextState.isUndoAvailable
//		) {
//			setSceneText(sceneTextState.undo())
//		}
//		EditorAction(
//			iconRes = MR.images.icon_redo,
//			active = sceneTextState.isRedoAvailable
//		) {
//			setSceneText(sceneTextState.redo())
//		}

		EditorAction(
			iconRes = MR.images.icon_text_decrease,
			active = false,
		) {
			decreaseTextSize()
		}
		EditorAction(
			iconRes = MR.images.icon_text_increase,
			active = false,
		) {
			increaseTextSize()
		}
		EditorAction(
			iconRes = MR.images.icon_text_reset,
			active = false,
		) {
			resetTextSize()
		}
	}
}