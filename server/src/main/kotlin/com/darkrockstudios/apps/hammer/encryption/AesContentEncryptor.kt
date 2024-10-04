package com.darkrockstudios.apps.hammer.encryption

import dev.whyoleg.cryptography.algorithms.symmetric.AES
import kotlinx.coroutines.runBlocking
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AesContentEncryptor(
	private val aesKeyProvider: AesKeyProvider,
) : ContentEncryptor {
	private val aesGcmKey: AES.GCM.Key by lazy {
		runBlocking {
			aesKeyProvider.getEncryptionKey("nothing-yet")
		}
	}
	private val cipher by lazy {
		aesGcmKey.cipher()
	}

	@OptIn(ExperimentalEncodingApi::class)
	override suspend fun encrypt(plainText: String, salt: String): String {
		val encryptedBytes = cipher.encrypt(plainText.toByteArray())
		val encryptedText = Base64.encode(encryptedBytes)
		return encryptedText
	}

	@OptIn(ExperimentalEncodingApi::class)
	override suspend fun decrypt(encrypted: String, salt: String): String {
		val encryptedBytes = Base64.decode(encrypted.encodeToByteArray())
		val plainTextBytes = cipher.decrypt(encryptedBytes)
		return plainTextBytes.toString(Charsets.UTF_8)
	}
}