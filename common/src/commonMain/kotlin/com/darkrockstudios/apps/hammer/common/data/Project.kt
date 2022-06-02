package com.darkrockstudios.apps.hammer.common.data

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Parcelize
data class Project(val name: String, val path: String): Parcelable