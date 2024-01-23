package com.darkrockstudios.apps.hammer.common.storyeditor.focusmode

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.darkrockstudios.apps.hammer.common.components.storyeditor.focusmode.FocusMode

@Composable
fun FocusModeUi(component: FocusMode) {
	Column {
		Button(onClick = { component.dismiss() }) {
			Text("Close")
		}
		Text("Focus mode")
	}
}