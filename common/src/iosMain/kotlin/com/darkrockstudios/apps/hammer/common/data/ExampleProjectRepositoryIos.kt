package com.darkrockstudios.apps.hammer.common.data

import com.darkrockstudios.apps.hammer.common.data.globalsettings.GlobalSettingsRepository
import kotlinx.cinterop.*
import kotlinx.cinterop.convert
import kotlinx.cinterop.toCValues
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.darwin.COMPRESSION_ZLIB
import platform.darwin.compression_decode_buffer
import platform.darwin.compression_encode_buffer
import kotlin.io.encoding.ExperimentalEncodingApi

actual val exampleProjectModule = module {
	singleOf(::ExampleProjectRepositoryiOs) bind ExampleProjectRepository::class
}

private class ExampleProjectRepositoryiOs(
	globalSettingsRepository: GlobalSettingsRepository,
) : ExampleProjectRepository(globalSettingsRepository) {

	override fun removeExampleProject() {
		// TODO("Not yet implemented")
	}

	override fun platformInstall() {
		// TODO("Not yet implemented")
	}
}

data class CompressionResponse(
	val base64EncodedString: String,
)

data class CompressionRequest(
	val data: String
)

data class DecompressionRequest(
	val base64EncodedString: String
)

data class DecompressionResponse(
	val data: String
)

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalEncodingApi::class)
object Compressor {
	private const val capacity = 10_000_000 // 10 MB, needs to be tuned

	@OptIn(ExperimentalForeignApi::class)
	fun compress(request: CompressionRequest): CompressionResponse? {
		return try {
			memScoped {
				val inputData = request.data.encodeToByteArray()
				val destinationBuffer = allocArray<UByteVar>(capacity)

				val newSize = compression_encode_buffer(
					destinationBuffer, capacity.convert(),
					inputData.toUByteArray().toCValues(), inputData.size.convert(),
					null,
					COMPRESSION_ZLIB
				)

				val bytes = destinationBuffer.readBytes(newSize.convert())
				CompressionResponse(base64EncodedString)
			}
		} catch (e: Exception) {
			null
		}
	}

	fun decompress(request: DecompressionRequest): DecompressionResponse? {
		return try {
			memScoped {
				val input = Base64.Default.decode(request.base64EncodedString).also { println(it.size) }

				val destinationBuffer = allocArray<UByteVar>(capacity)
				val oldSize = compression_decode_buffer(
					destinationBuffer, capacity.convert(),
					input.toUByteArray().toCValues(), input.size.convert(),
					null,
					COMPRESSION_ZLIB
				)

				val normalString = destinationBuffer.readBytes(oldSize.convert()).decodeToString()
				DecompressionResponse(normalString)
			}
		} catch (e: Exception) {
			null
		}
	}
}