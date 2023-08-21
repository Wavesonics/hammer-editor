package com.darkrockstudios.apps.hammer.common.projectselection

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.useResource
import com.darkrockstudios.apps.hammer.common.compose.MpDialog
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer

@Composable
fun LibrariesUi(
	showLibraries: Boolean,
	close: () -> Unit
) {
	MpDialog(
		onCloseRequest = close,
		visible = showLibraries,
		title = "Libraries",
	) {
		LibrariesContainer(useResource("aboutlibraries.json") {
			it.bufferedReader().readText()
		}, Modifier.fillMaxSize())
	}
}