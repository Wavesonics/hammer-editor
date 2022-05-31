package com.darkrockstudios.apps.hammer.common.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.router
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.darkrockstudios.apps.hammer.common.counter.Counter

class RootComponent(
    componentContext: ComponentContext
) : Root, ComponentContext by componentContext {

    private val router: Router<Config, Root.Screen> =
        router(
            initialConfiguration = Config.Counter(),
            childFactory = ::createChild
        )

    override val routerState: Value<RouterState<*, Root.Screen>> = router.state

    private fun createChild(config: Config, componentContext: ComponentContext): Root.Screen =
        when (config) {
            is Config.Counter -> Root.Screen.CounterScreen(Counter(componentContext, config.start))
        }

    private sealed class Config : Parcelable {
        @Parcelize
        class Counter(val start: Int = 0) : Config()
    }
}