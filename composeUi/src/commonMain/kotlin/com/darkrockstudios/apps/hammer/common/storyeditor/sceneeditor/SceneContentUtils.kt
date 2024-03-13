package com.darkrockstudios.apps.hammer.common.storyeditor.sceneeditor

import com.darkrockstudios.apps.hammer.common.compose.ComposeRichText
import com.darkrockstudios.apps.hammer.common.data.SceneContent
import com.mohamedrejeb.richeditor.model.RichTextState

fun getInitialEditorContent(sceneContent: SceneContent?): RichTextState {
	return if (sceneContent != null) {
		val composeText = sceneContent.platformRepresentation as? ComposeRichText
		val markdown = sceneContent.markdown
		if (composeText != null) {
			composeText.state
		} else if (markdown != null) {
			val newState = RichTextState()
			newState.setMarkdown(markdown)
			newState
		} else {
			throw IllegalStateException("Should be impossible to not have either platform rep or markdown")
		}
	} else {
		RichTextState()
	}
}