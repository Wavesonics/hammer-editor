package com.darkrockstudios.apps.hammer.common.components.projectroot

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.statekeeper.polymorphicSerializer
import com.darkrockstudios.apps.hammer.common.components.projectsync.ProjectSyncComponent
import com.darkrockstudios.apps.hammer.common.data.ProjectDef
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

class ProjectRootModalRouter(
	componentContext: ComponentContext,
	private val projectDef: ProjectDef,
) : Router {
	private val navigation = SlotNavigation<Config>()

	val state: Value<ChildSlot<Config, ProjectRoot.ModalDestination>> =
		componentContext.childSlot(
			source = navigation,
			initialConfiguration = { Config.None },
			key = "ProjectRootModalRouter",
			childFactory = ::createChild,
			serializer = ConfigSerializer,
		)

	override fun isAtRoot(): Boolean {
		return state.value.child?.instance is ProjectRoot.ModalDestination.None
	}

	override fun shouldConfirmClose() = emptySet<CloseConfirm>()

	private fun createChild(
		config: Config,
		componentContext: ComponentContext
	): ProjectRoot.ModalDestination =
		when (config) {
			Config.None -> ProjectRoot.ModalDestination.None
			Config.ProjectSync -> ProjectRoot.ModalDestination.ProjectSync(
				ProjectSyncComponent(componentContext, projectDef, ::dismissProjectSync)
			)
		}

	fun showProjectSync() {
		navigation.activate(Config.ProjectSync)
	}

	fun dismissProjectSync() {
		navigation.activate(Config.None)
	}

	@Serializable
	sealed class Config {
		@Serializable
		data object None : Config()

		@Serializable
		data object ProjectSync : Config()
	}

	private object ConfigSerializer : KSerializer<Config> by polymorphicSerializer(
		SerializersModule {
			polymorphic(Config::class) {
				subclass(Config.None::class, Config.None.serializer())
				subclass(Config.ProjectSync::class, Config.ProjectSync.serializer())
			}
		}
	)
}