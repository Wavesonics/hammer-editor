package com.darkrockstudios.apps.hammer.encryption

import dev.whyoleg.cryptography.algorithms.symmetric.AES

interface AesKeyProvider {
	suspend fun getEncryptionKey(salt: String): AES.GCM.Key
}