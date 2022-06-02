package com.darkrockstudios.apps.hammer.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color
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
            val component = ProjectEditorComponent(
                componentContext = defaultComponentContext(),
                project = project
            )

            setContent {
                MaterialTheme {
                    val scaffoldState = rememberScaffoldState()
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            TopAppBar(
                                title = { Text("Hammer") },
                                backgroundColor = Color.Red,
                                elevation = Ui.ELEVATION,
                                navigationIcon = {
                                    IconButton(onClick = ::onBackPressed) {
                                        Icon(Icons.Filled.ArrowBack, "backIcon")
                                    }
                                }
                            )
                        },
                        content = {
                            ProjectEditorUi(component)
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