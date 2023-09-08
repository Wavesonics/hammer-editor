package com.darkrockstudios.apps.hammer.common.encyclopedia

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.darkrockstudios.apps.hammer.common.components.encyclopedia.Encyclopedia

@Composable
fun EncyclopediaUi(
	component: Encyclopedia,
	snackbarHostState: SnackbarHostState,
	modifier: Modifier = Modifier,
) {
	val scope = rememberCoroutineScope()
	val state by component.stack.subscribeAsState()

	BoxWithConstraints(modifier = modifier.fillMaxSize()) {
		Children(
			stack = state,
			modifier = Modifier,
			animation = stackAnimation { _ -> fade() },
		) {
			when (val child = it.instance) {
				is Encyclopedia.Destination.BrowseEntriesDestination -> {
					BrowseEntriesUi(
						component = child.component,
						scope = scope,
						viewEntry = component::showViewEntry
					)
				}

				is Encyclopedia.Destination.ViewEntryDestination -> {
					ViewEntryUi(
						modifier = Modifier.align(Alignment.TopCenter),
						component = child.component,
						scope = scope,
						snackbarHostState = snackbarHostState,
						closeEntry = component::showBrowse
					)
				}

				is Encyclopedia.Destination.CreateEntryDestination -> {
					CreateEntryUi(
						component = child.component,
						scope = scope,
						modifier = Modifier.align(Alignment.TopCenter),
						snackbarHostState = snackbarHostState,
						close = component::showBrowse
					)
				}
			}
		}
	}
}