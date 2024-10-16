package com.darkrockstudios.apps.hammer.common.dependencyinjection

import com.darkrockstudios.apps.hammer.common.compose.ImageLoaderNapierLogger
import com.darkrockstudios.apps.hammer.common.compose.NullDataInterceptor
import com.darkrockstudios.apps.hammer.common.getImageCacheDirectory
import com.eygraber.uri.Uri
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.component.decoder.SkiaImageDecoder
import com.seiko.imageloader.component.mapper.Mapper
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.intercept.bitmapMemoryCacheConfig
import com.seiko.imageloader.intercept.imageMemoryCacheConfig
import com.seiko.imageloader.intercept.painterMemoryCacheConfig
import com.seiko.imageloader.option.Options
import com.seiko.imageloader.util.LogPriority
import okio.FileSystem
import okio.Path.Companion.toPath

internal fun generateImageLoader(fileSystem: FileSystem): ImageLoader {
	return ImageLoader {
		components {
			setupDefaultComponents()
			add(WindowsLocalFileUriMapper())
			add(SkiaImageDecoder.Factory())
		}
		logger = ImageLoaderNapierLogger(LogPriority.WARN)
		interceptor {
			addInterceptor(NullDataInterceptor)
			// cache 32MB bitmap
			bitmapMemoryCacheConfig {
				maxSize(32 * 1024 * 1024) // 32MB
			}
			// cache 50 image
			imageMemoryCacheConfig {
				maxSize(50)
			}
			// cache 50 painter
			painterMemoryCacheConfig {
				maxSize(50)
			}
			diskCacheConfig {
				directory(getImageCacheDirectory().toPath())
				maxSizeBytes(512L * 1024 * 1024) // 512MB
			}
		}
	}
}

private class WindowsLocalFileUriMapper : Mapper<okio.Path> {
	override fun map(data: Any, options: Options): okio.Path? {
		if (data !is Uri) return null
		if (!isApplicable(data)) return null
		return data.toString().toPath()
	}

	private val pattern = Regex("""^[a-zA-Z]$""")

	private fun isApplicable(data: Uri): Boolean {
		return pattern.matches(data.scheme ?: "")
	}
}