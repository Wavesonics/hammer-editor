package com.darkrockstudios.apps.hammer.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import com.darkrockstudios.apps.hammer.common.Ui
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.projecteditor.ProjectEditorComponent
import com.darkrockstudios.apps.hammer.common.projecteditor.ProjectEditorUi

class ProjectEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val project = intent.getParcelableExtra<Project>(EXTRA_PROJECT)
        if (project == null) {
            finish()
        } else {
            setContent {
                MaterialTheme {
                    val scaffoldState = rememberScaffoldState()
                    val topBar = TopAppBar(
                        title = { Text("Hammer") },
                        backgroundColor = Ui.Colors.ACCENT,
                        elevation = Ui.ELEVATION,
                        navigationIcon = {
                            IconButton(onClick = ::onBackPressed) {
                                Icon(Icons.Filled.ArrowBack, "backIcon")
                            }
                        },
                        actions = {

                        }
                    )
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            topBar
                        },
                        content = { padding ->
                            ProjectEditorUi(
                                ProjectEditorComponent(
                                    componentContext = defaultComponentContext(),
                                    project = project,
                                    addMenu = {

                                    },
                                    removeMenu = {

                                    }
                                ),
                                Modifier.padding(padding),
                            )
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_PROJECT = "project"
    }
}