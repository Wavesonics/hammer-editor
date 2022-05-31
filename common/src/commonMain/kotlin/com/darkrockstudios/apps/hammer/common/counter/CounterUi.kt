package com.darkrockstudios.apps.hammer.common.counter

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.darkrockstudios.apps.hammer.common.counter.Counter

@Composable
fun CounterUi(component: Counter, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()

    Column(modifier = modifier) {
        Text(text = state.count.toString())

        Button(onClick = component::increment) {
            Text("Increment")
        }
    }
}