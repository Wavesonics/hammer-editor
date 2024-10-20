package com.darkrockstudios.apps.hammer.common.projectselection

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.common.components.projectselection.projectslist.ProjectsList
import com.darkrockstudios.apps.hammer.common.compose.SimpleDialog
import com.darkrockstudios.apps.hammer.common.compose.SpacerL
import com.darkrockstudios.apps.hammer.common.compose.Ui
import com.darkrockstudios.apps.hammer.common.compose.moko.get

@Composable
fun ProjectCreateDialog(show: Boolean, component: ProjectsList, close: () -> Unit) {
	SimpleDialog(
		onCloseRequest = close,
		visible = show,
		title = MR.strings.create_project_title.get(),
	) {
		val state by component.state.subscribeAsState()
		Box(modifier = Modifier.fillMaxWidth().padding(Ui.Padding.XL)) {
			Column(
				modifier = Modifier
					.width(IntrinsicSize.Max)
					.align(Alignment.Center)
			) {
				TextField(
					value = state.createDialogProjectName,
					onValueChange = { component.onProjectNameUpdate(it) },
					label = { Text(MR.strings.create_project_heading.get()) },
					singleLine = true,
				)

				SpacerL()

				Button(onClick = {
					component.createProject(state.createDialogProjectName)
				}) {
					Text(MR.strings.create_project_button.get())
				}
			}
		}
	}
}