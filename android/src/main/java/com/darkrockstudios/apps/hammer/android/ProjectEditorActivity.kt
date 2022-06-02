package com.darkrockstudios.apps.hammer.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import com.arkivanov.decompose.defaultComponentContext
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
                    ProjectEditorUi(component)
                }
            }
        }
    }

    companion object {
        const val EXTRA_PROJECT = "project"
    }
}