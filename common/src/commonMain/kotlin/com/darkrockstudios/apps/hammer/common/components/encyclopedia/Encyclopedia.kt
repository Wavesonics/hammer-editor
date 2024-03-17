package com.darkrockstudios.apps.hammer.common.components.encyclopedia

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.statekeeper.polymorphicSerializer
import com.darkrockstudios.apps.hammer.common.components.projectroot.Router
import com.darkrockstudios.apps.hammer.common.data.ProjectDef
import com.darkrockstudios.apps.hammer.common.data.encyclopediarepository.entry.EntryDef
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

interface Encyclopedia : Router {
	val stack: Value<ChildStack<Config, Destination>>

	sealed class Destination {
		data class BrowseEntriesDestination(val component: BrowseEntries) : Destination()

		data class ViewEntryDestination(val component: ViewEntry) : Destination()

		data class CreateEntryDestination(val component: CreateEntry) : Destination()
	}

	@Serializable
	sealed class Config {
		@Serializable
		data class BrowseEntriesConfig(val projectDef: ProjectDef) : Config()

		@Serializable
		data class ViewEntryConfig(val entryDef: EntryDef) : Config()

		@Serializable
		data class CreateEntryConfig(val projectDef: ProjectDef) : Config()
	}

	object ConfigSerializer : KSerializer<Config> by polymorphicSerializer(
		SerializersModule {
			polymorphic(Config::class) {
				subclass(Config.BrowseEntriesConfig::class, Config.BrowseEntriesConfig.serializer())
				subclass(Config.ViewEntryConfig::class, Config.ViewEntryConfig.serializer())
				subclass(Config.CreateEntryConfig::class, Config.CreateEntryConfig.serializer())
			}
		}
	)

	fun showBrowse()
	fun showViewEntry(entryDef: EntryDef)
	fun showCreateEntry()
}