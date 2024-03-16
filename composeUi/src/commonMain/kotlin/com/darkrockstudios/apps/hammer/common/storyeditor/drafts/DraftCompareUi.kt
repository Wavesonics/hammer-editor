@file:OptIn(ExperimentalMaterial3Api::class)

package com.darkrockstudios.apps.hammer.common.storyeditor.drafts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.common.components.storyeditor.drafts.DraftCompare
import com.darkrockstudios.apps.hammer.common.compose.ComposeRichText
import com.darkrockstudios.apps.hammer.common.compose.LocalScreenCharacteristic
import com.darkrockstudios.apps.hammer.common.compose.Ui
import com.darkrockstudios.apps.hammer.common.compose.moko.get
import com.darkrockstudios.apps.hammer.common.compose.rememberStrRes
import com.darkrockstudios.apps.hammer.common.storyeditor.sceneeditor.getInitialEditorContent
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

@Composable
fun DraftCompareUi(component: DraftCompare) {
	val screen = LocalScreenCharacteristic.current

	Column(modifier = Modifier.fillMaxSize()) {
		if (LocalScreenCharacteristic.current.needsExplicitClose) {
			IconButton(
				onClick = { component.cancel() },
				modifier = Modifier.align(Alignment.End)
			) {
				Icon(
					Icons.Default.Cancel,
					contentDescription = MR.strings.draft_compare_cancel_button.get(),
					tint = MaterialTheme.colorScheme.onBackground
				)
			}
		}

		when (screen.windowWidthClass) {
			WindowWidthSizeClass.Compact, WindowWidthSizeClass.Medium -> {
				CompactDraftCompareUi(Modifier.fillMaxSize(), component)
			}

			else -> {
				ExpandedDraftCompareUi(Modifier.fillMaxSize(), component)
			}
		}
	}
}

@Composable
private fun CompactDraftCompareUi(modifier: Modifier, component: DraftCompare) {
	var tabState by rememberSaveable { mutableStateOf(0) }
	val titles = remember {
		listOf(MR.strings.draft_compare_tab_title_draft, MR.strings.draft_compare_tab_title_current)
	}

	Column(modifier = modifier) {
		TabRow(selectedTabIndex = tabState) {
			titles.forEachIndexed { index, title ->
				Tab(
					text = { Text(title.get()) },
					selected = tabState == index,
					onClick = { tabState = index }
				)
			}
		}
		if (tabState == 0) {
			DraftContent(
				modifier = Modifier.weight(1f),
				component = component
			)
		} else if (tabState == 1) {
			CurrentContent(
				modifier = Modifier.weight(1f),
				component = component
			)
		}
	}
}

@Composable
private fun ExpandedDraftCompareUi(modifier: Modifier, component: DraftCompare) {
	Row(modifier = modifier) {
		DraftContent(
			modifier = Modifier.weight(1f),
			component = component,
		)

		CurrentContent(
			modifier = Modifier.weight(1f),
			component = component
		)
	}
}

@Composable
private fun CurrentContent(
	modifier: Modifier,
	component: DraftCompare
) {
	val state by component.state.subscribeAsState()

	// I feel like there must be a better way...
	val textState by remember(state.sceneContent) {
		val existing = state.mergedContent as? ComposeRichText

		if (existing == null && state.sceneContent != null) {
			val sceneState = (state.sceneContent?.platformRepresentation as? ComposeRichText)?.state
			if (sceneState != null) {
				component.onMergedContentChanged(ComposeRichText(sceneState))
			}

			mutableStateOf(
				sceneState ?: RichTextState()
			)
		} else {
			mutableStateOf(
				existing?.state ?: RichTextState()
			)
		}
	}

	LaunchedEffect(textState.annotatedString) {
		component.onMergedContentChanged(ComposeRichText(textState))
	}

	Card(
		modifier = modifier.padding(Ui.Padding.L),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiaryContainer),
		elevation = CardDefaults.outlinedCardElevation(
			defaultElevation = Ui.Elevation.MEDIUM
		),
	) {
		Column(modifier = Modifier.padding(Ui.Padding.L)) {
			Text(
				MR.strings.draft_compare_current_header.get(),
				style = MaterialTheme.typography.headlineLarge
			)
			Text(
				MR.strings.draft_compare_current_subheader.get(),
				style = MaterialTheme.typography.bodySmall,
				fontStyle = FontStyle.Italic
			)

			Button(onClick = { component.pickMerged() }) {
				Text(MR.strings.draft_compare_current_accept_button.get())
			}

			com.mohamedrejeb.richeditor.ui.material3.RichTextEditor(
				modifier = Modifier.fillMaxSize(),
				state = textState,
				placeholder = {
					androidx.compose.material.Text(
						MR.strings.draft_compare_current_body_placeholder.get(),
						color = MaterialTheme.colorScheme.onBackground,
					)
				},
				shape = RectangleShape,
				colors = RichTextEditorDefaults.richTextEditorColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
				),
				textStyle = MaterialTheme.typography.bodyLarge.copy(
					//fontSize = state.textSize.sp,
					color = MaterialTheme.colorScheme.onBackground,
				),
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DraftContent(
	modifier: Modifier,
	component: DraftCompare,
) {
	val strRes = rememberStrRes()
	val state by component.state.subscribeAsState()
	var draftText by remember(state.draftContent) { mutableStateOf(getInitialEditorContent(state.draftContent)) }

	Card(modifier = modifier.padding(Ui.Padding.L)) {
		Column(modifier = Modifier.padding(Ui.Padding.L)) {

			/*
			val date = remember(component.draftDef.draftTimestamp) {
				val created = component.draftDef.draftTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
				created.format("dd MMM `yy")
			}
			*/

			Text(
				strRes.get(MR.strings.draft_compare_draft_header, component.draftDef.draftName),
				style = MaterialTheme.typography.headlineLarge
			)
			Text(
				MR.strings.draft_compare_draft_subheader.get(),
				style = MaterialTheme.typography.bodySmall,
				fontStyle = FontStyle.Italic
			)
			Button(onClick = { component.pickDraft() }) {
				Text(MR.strings.draft_compare_draft_accept_button.get())
			}

			com.mohamedrejeb.richeditor.ui.material3.RichTextEditor(
				modifier = Modifier.fillMaxSize(),
				state = draftText,
				placeholder = {
					androidx.compose.material.Text(
						MR.strings.draft_compare_draft_body_placeholder.get(),
						color = MaterialTheme.colorScheme.onBackground,
					)
				},
				shape = RectangleShape,
				colors = RichTextEditorDefaults.richTextEditorColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
				),
				textStyle = MaterialTheme.typography.bodyLarge.copy(
					//fontSize = state.textSize.sp,
					color = MaterialTheme.colorScheme.onBackground,
				),
				readOnly = false,
			)
		}
	}
}