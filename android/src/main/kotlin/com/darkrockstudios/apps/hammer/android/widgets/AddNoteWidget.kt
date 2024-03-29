package com.darkrockstudios.apps.hammer.android.widgets

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.darkrockstudios.apps.hammer.android.R
import com.darkrockstudios.apps.hammer.common.compose.theme.DarkColors
import com.darkrockstudios.apps.hammer.common.compose.theme.LightColors
import io.github.aakira.napier.Napier

class AddNoteWidget : GlanceAppWidget() {

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val glanceAppWidgetManager = GlanceAppWidgetManager(context)
		provideContent {
			val widgetId = remember { glanceAppWidgetManager.getAppWidgetId(id) }
			val data by context.widgetConfigDataStore.data.collectAsState(initial = null)
			val projectName = remember(data) { data?.getWidgetConfig(widgetId) }

			GlanceTheme(colors = HammerWidgetGlanceColorScheme.colors) {
				GlanceTheme.colors.surface
				Box(
					modifier = GlanceModifier
						.fillMaxSize()
						// TODO why can't we pass a ColorProvider here?
						.background(
							day = LightColors.surface,
							night = DarkColors.surface
						)
						.appWidgetBackground()
						.cornerRadius(16.dp)
						.padding(8.dp)
						.clickable(getAddNoteActionCallback(projectName))
				) {
					Column(
						modifier = GlanceModifier.fillMaxSize(),
						horizontalAlignment = Alignment.Horizontal.CenterHorizontally
					) {
						Image(
							ImageProvider(resId = R.drawable.ic_add_note),
							contentDescription = context.getString(R.string.note_widget_button_description),
							colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
							modifier = GlanceModifier
								.clickable(getAddNoteActionCallback(projectName))
								.fillMaxWidth()
								.defaultWeight()
						)

						if (!projectName.isNullOrBlank()) {
							Text(
								projectName,
								maxLines = 1,
								style = TextStyle(
									color = GlanceTheme.colors.onSurface,
									textAlign = TextAlign.Center
								)
							)
						} else {
							Text(
								context.getString(R.string.note_widget_button_description),
								style = TextStyle(GlanceTheme.colors.onSurface)
							)
						}
					}
				}
			}
		}
	}

	private fun getAddNoteActionCallback(projectName: String?): Action {
		return actionRunCallback<AddNoteClickAction>(
			actionParametersOf(AddNoteActionParameterKey to (projectName ?: ""))
		)
	}
}

private const val ACTION_KEY_PROJECT_NAME = "project_name"
private val AddNoteActionParameterKey = ActionParameters.Key<String>(ACTION_KEY_PROJECT_NAME)

class AddNoteWidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = AddNoteWidget()
}

class AddNoteClickAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters
	) {
		val projectName = parameters[AddNoteActionParameterKey]

		Napier.d { "Add Note widget tapped for project: `$projectName`" }

		val intent = Intent(context, AddNoteActivity::class.java)
			.setFlags(FLAG_ACTIVITY_NEW_TASK)
			.putExtra(AddNoteActivity.EXTRA_PROJECT_NAME, projectName)
		context.startActivity(intent)
	}
}

object HammerWidgetGlanceColorScheme {
	val colors = ColorProviders(
		light = LightColors,
		dark = DarkColors
	)
}