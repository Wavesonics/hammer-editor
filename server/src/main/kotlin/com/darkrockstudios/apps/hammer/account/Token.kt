package com.darkrockstudios.apps.hammer.account

@JvmInline
value class Token(val value: String) {

    fun isValid(): Boolean {
        return when {
            value.length != LENGTH -> false
            !value.matches(characters) -> false
            else -> true
        }
    }

    companion object {
        private val characters = Regex("[abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789]+")
        const val LENGTH = 64
    }
}