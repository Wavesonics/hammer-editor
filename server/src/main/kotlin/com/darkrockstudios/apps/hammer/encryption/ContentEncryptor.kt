package com.darkrockstudios.apps.hammer.encryption

interface ContentEncryptor {
	suspend fun encrypt(plainText: String, salt: String): String
	suspend fun decrypt(encrypted: String, salt: String): String
}