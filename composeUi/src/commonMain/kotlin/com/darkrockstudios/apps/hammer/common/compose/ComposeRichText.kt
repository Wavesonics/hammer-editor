package com.darkrockstudios.apps.hammer.common.compose

import com.darkrockstudios.apps.hammer.common.data.PlatformRichText
import com.mohamedrejeb.richeditor.model.RichTextState

data class ComposeRichText(val state: RichTextState) : PlatformRichText {
	override fun convertToMarkdown() = state.toMarkdown()

	override fun compare(text: PlatformRichText): Boolean {
		return if (text is ComposeRichText) {
			text.state.annotatedString == state.annotatedString
		} else {
			false
		}
	}

	override fun equals(other: Any?): Boolean {
		return if (other is PlatformRichText) {
			compare(other)
		} else {
			false
		}
	}

	override fun hashCode(): Int {
		return state.annotatedString.hashCode()
	}
}