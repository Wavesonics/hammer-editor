package com.darkrockstudios.apps.hammer.common.projectselection

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.darkrockstudios.apps.hammer.common.compose.MpDialog
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer

@Composable
actual fun LibrariesUi(
	showLibraries: Boolean,
	close: () -> Unit
) {
	MpDialog(
		onCloseRequest = close,
		visible = showLibraries,
		title = "Libraries",
	) {
		LibrariesContainer(
			modifier = Modifier.fillMaxSize()
		)
	}
}