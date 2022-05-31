package com.darkrockstudios.apps.hammer.android

import com.darkrockstudios.apps.hammer.common.App
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import com.arkivanov.decompose.defaultComponentContext
import com.darkrockstudios.apps.hammer.common.root.RootComponent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RootComponent(componentContext = defaultComponentContext())

        setContent {
            MaterialTheme {
                App(root)
            }
        }
    }
}