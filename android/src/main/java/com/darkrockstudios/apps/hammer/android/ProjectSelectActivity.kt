package com.darkrockstudios.apps.hammer.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import com.darkrockstudios.apps.hammer.common.Ui
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.projectselection.ProjectSelectionComponent
import com.darkrockstudios.apps.hammer.common.projectselection.ProjectSelectionUi

class ProjectSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = ProjectSelectionComponent(
            componentContext = defaultComponentContext(),
            onProjectSelected = ::onProjectSelected
        )

        setContent {
            MaterialTheme {
                val scaffoldState = rememberScaffoldState()
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TopAppBar(
                            title = { Text("Hammer") },
                            elevation = Ui.ELEVATION,
                        )
                    },
                    content = { padding ->
                        ProjectSelectionUi(component, Modifier.padding(padding))
                    }
                )
            }
        }
    }

    private fun onProjectSelected(project: Project) {
        val intent = Intent(this, ProjectEditorActivity::class.java).apply {
            putExtra(ProjectEditorActivity.EXTRA_PROJECT, project)
        }
        startActivity(intent)
    }
}