package com.darkrockstudios.apps.hammer.common.data

data class MenuDescriptor(val id: String, val label: String, val items: List<MenuItemDescriptor>)

data class MenuItemDescriptor(
    val id: String,
    val label: String,
    val icon: String,
    val action: (itemId: String) -> Unit
)