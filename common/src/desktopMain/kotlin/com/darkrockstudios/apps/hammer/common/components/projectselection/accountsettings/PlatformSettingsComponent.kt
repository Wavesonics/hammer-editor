package com.darkrockstudios.apps.hammer.common.components.projectselection.accountsettings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.darkrockstudios.apps.hammer.common.components.SavableComponent

class DesktopSettingsComponent(componentContext: ComponentContext): PlatformSettings,
	SavableComponent<DesktopSettingsComponent.PlatformState>(componentContext){

	private val _state = MutableValue(PlatformState())
	override val state: Value<PlatformState> = _state

	@Parcelize
	class PlatformState(
	) : Parcelable
}