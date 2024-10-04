package com.darkrockstudios.apps.hammer.encryption

import com.darkrockstudios.apps.hammer.utilities.getRootDataDirectory
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.symmetric.AES
import dev.whyoleg.cryptography.algorithms.symmetric.SymmetricKeySize
import okio.FileSystem
import java.io.IOException

class SimpleAesKeyProvider(
	private val fileSystem: FileSystem,
) : AesKeyProvider {
	private val SERVER_KEY_FILE_NAME = "server.key"

	private val provider = CryptographyProvider.Default
	private val aesGcm = provider.get(AES.GCM)
	private val keyGenerator = aesGcm.keyGenerator(SymmetricKeySize.B256)

	private val keyPath = getRootDataDirectory(fileSystem) / SERVER_KEY_FILE_NAME
	private var cachedKey: AES.GCM.Key? = null

	private suspend fun getKey(): AES.GCM.Key {
		return cachedKey ?: try {
			fileSystem.createDirectories(keyPath.parent ?: error("Parent does not exist"))
			loadFromDisk()?.also { cachedKey = it } ?: run {
				val newKey = keyGenerator.generateKey()
				saveToDisk(newKey)
				cachedKey = newKey
				newKey
			}
		} catch (e: IOException) {
			println("Well we're about to have a bad day")
			throw KeyLoadingException("Failed to load or generate key", e)
		}
	}

	private suspend fun loadFromDisk(): AES.GCM.Key? {
		return if (fileSystem.exists(keyPath)) {
			val keyPath = getRootDataDirectory(fileSystem) / SERVER_KEY_FILE_NAME
			return fileSystem.read(keyPath) {
				val keyBytes = readByteArray()
				aesGcm.keyDecoder().decodeFrom(AES.Key.Format.RAW, keyBytes)
			}
		} else {
			null
		}
	}

	private suspend fun saveToDisk(key: AES.GCM.Key) {
		val keyPath = getRootDataDirectory(fileSystem) / SERVER_KEY_FILE_NAME
		val keyBytes = key.encodeTo(AES.Key.Format.RAW)
		fileSystem.write(keyPath) {
			write(keyBytes)
		}
	}

	override suspend fun getEncryptionKey(salt: String): AES.GCM.Key = getKey()
}

class KeyLoadingException(message: String, cause: Throwable) : Exception(message, cause)