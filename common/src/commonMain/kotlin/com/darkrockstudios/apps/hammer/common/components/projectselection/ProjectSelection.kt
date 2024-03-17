package com.darkrockstudios.apps.hammer.common.components.projectselection

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.statekeeper.polymorphicSerializer
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.common.components.projectselection.aboutapp.AboutApp
import com.darkrockstudios.apps.hammer.common.components.projectselection.accountsettings.AccountSettings
import com.darkrockstudios.apps.hammer.common.components.projectselection.projectslist.ProjectsList
import com.darkrockstudios.apps.hammer.common.dependencyinjection.HammerComponent
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

interface ProjectSelection : HammerComponent {
	val slot: Value<ChildSlot<Config, Destination>>

	val showProjectDirectory: Boolean

	fun showLocation(location: Locations)

	enum class Locations(val text: StringResource) {
		Projects(MR.strings.project_select_nav_projects_list),
		Settings(MR.strings.project_select_nav_account_settings),
		AboutApp(MR.strings.project_select_nav_about_app),
	}

	@Serializable
	sealed class Config(val location: Locations) {
		@Serializable
		data object ProjectsList : Config(Locations.Projects)

		@Serializable
		data object AccountSettings : Config(Locations.Settings)

		@Serializable
		data object AboutApp : Config(Locations.AboutApp)
	}

	object ConfigSerializer : KSerializer<Config> by polymorphicSerializer(
		SerializersModule {
			polymorphic(Config::class) {
				subclass(Config.ProjectsList::class, Config.ProjectsList.serializer())
				subclass(Config.AccountSettings::class, Config.AccountSettings.serializer())
				subclass(Config.AboutApp::class, Config.AboutApp.serializer())
			}
		}
	)

	sealed class Destination {
		data class ProjectsListDestination(val component: ProjectsList) : Destination()
		data class AccountSettingsDestination(val component: AccountSettings) : Destination()
		data class AboutAppDestination(val component: AboutApp) : Destination()
	}
}