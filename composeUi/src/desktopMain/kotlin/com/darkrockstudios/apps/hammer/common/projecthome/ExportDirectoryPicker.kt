package com.darkrockstudios.apps.hammer.common.projecthome

import androidx.compose.runtime.Composable
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.common.components.projecthome.ProjectHome
import com.darkrockstudios.apps.hammer.common.compose.RootSnackbarHostState
import com.darkrockstudios.apps.hammer.common.compose.rememberDefaultDispatcher
import com.darkrockstudios.apps.hammer.common.compose.rememberStrRes
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
actual fun ExportDirectoryPicker(
	show: Boolean,
	component: ProjectHome,
	scope: CoroutineScope,
	rootSnackbar: RootSnackbarHostState,
) {
	val strRes = rememberStrRes()
	val defaultDispatcher = rememberDefaultDispatcher()

	DirectoryPicker(show) { path ->
		if (path != null) {
			scope.launch(defaultDispatcher) {
				component.exportProject(path)
				rootSnackbar.showSnackbar(strRes.get(MR.strings.project_home_action_export_toast_success))
			}
		} else {
			component.endProjectExport()
		}
	}
}
