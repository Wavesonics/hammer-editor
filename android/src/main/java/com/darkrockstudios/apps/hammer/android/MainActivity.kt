package com.darkrockstudios.apps.hammer.android

import com.darkrockstudios.apps.hammer.common.projectselection.ProjectSelectionComponent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import com.arkivanov.decompose.defaultComponentContext
import com.darkrockstudios.apps.hammer.common.data.Project
import com.darkrockstudios.apps.hammer.common.projecteditor.root.RootComponent
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Napier.base(DebugAntilog())

        val root = RootComponent(
            componentContext = defaultComponentContext(),
            onProjectSelected = ::onProjectSelected
        )

        setContent {
            MaterialTheme {
                ProjectSelectionComponent(compContext, onProjectSelected)
            }
        }
    }

    private fun onProjectSelected(project: Project) {

    }
}