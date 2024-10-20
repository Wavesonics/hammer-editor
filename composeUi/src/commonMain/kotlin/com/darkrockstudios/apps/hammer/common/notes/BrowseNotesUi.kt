package com.darkrockstudios.apps.hammer.common.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.common.components.notes.BrowseNotes
import com.darkrockstudios.apps.hammer.common.compose.HeaderUi
import com.darkrockstudios.apps.hammer.common.compose.Ui
import com.darkrockstudios.apps.hammer.common.compose.moko.get
import com.darkrockstudios.apps.hammer.common.data.notesrepository.note.NoteContent
import com.darkrockstudios.apps.hammer.common.util.format
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun BrowseNotesUi(component: BrowseNotes, modifier: Modifier) {
	val state by component.state.subscribeAsState()

	Column(modifier = modifier.padding(start = Ui.Padding.L, end = Ui.Padding.L, top = Ui.Padding.L)) {
		HeaderUi(MR.strings.notes_header, "\uD83D\uDCD1")

		LazyVerticalStaggeredGrid(
			columns = StaggeredGridCells.Adaptive(400.dp),
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(horizontal = Ui.Padding.XL)
		) {
			if (state.notes.isEmpty()) {
				item {
					Text(
						MR.strings.notes_list_empty.get(),
						style = MaterialTheme.typography.headlineSmall,
						color = MaterialTheme.colorScheme.onBackground
					)
				}
			}

			items(
				count = state.notes.size,
			) { index ->
				val note = state.notes[index]
				NoteItem(
					note = note,
				) {
					component.viewNote(note.id)
				}
			}
		}
	}
}

@Composable
fun NoteItem(
	note: NoteContent,
	modifier: Modifier = Modifier,
	viewNote: () -> Unit,
) {
	Card(
		modifier = modifier.fillMaxWidth().padding(Ui.Padding.XL).clickable { viewNote() },
		elevation = CardDefaults.elevatedCardElevation(Ui.Elevation.SMALL)
	) {
		Column(
			modifier = Modifier.padding(Ui.Padding.XL).fillMaxWidth()
		) {
			Row {
				Text(
					note.content,
					modifier = Modifier.weight(1f),
					style = MaterialTheme.typography.bodyMedium
						.copy(color = MaterialTheme.colorScheme.onBackground),
					maxLines = 12,
					overflow = TextOverflow.Ellipsis
				)
			}
			Spacer(modifier = Modifier.size(Ui.Padding.L))

			val date = remember(note.created) {
				val created = note.created.toLocalDateTime(TimeZone.currentSystemDefault())
				created.format("dd MMM `yy")
			}

			Text(
				date,
				style = MaterialTheme.typography.bodySmall
			)
		}
	}
}


@Composable
fun BrowseNotesFab(
	component: BrowseNotes,
	modifier: Modifier,
) {
	FloatingActionButton(
		modifier = modifier,
		onClick = { component.showCreate() },
	) {
		Icon(Icons.Filled.Create, MR.strings.notes_create_note_button.get())
	}
}