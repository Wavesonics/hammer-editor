package com.darkrockstudios.apps.hammer.common.root

import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.Value
import com.darkrockstudios.apps.hammer.common.counter.Counter

interface Root {

    val routerState: Value<RouterState<*, Screen>>

    sealed class Screen {
        class CounterScreen(val component: Counter) : Screen()
    }
}