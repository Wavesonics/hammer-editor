package com.darkrockstudios.apps.hammer.common

import com.darkrockstudios.apps.hammer.common.dependencyinjection.NapierLogger
import com.darkrockstudios.apps.hammer.common.dependencyinjection.mainModule
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import okio.FileSystem
import org.koin.core.context.startKoin
import kotlin.coroutines.CoroutineContext
import platform.Foundation.*

actual fun getPlatformName(): String {
	return "iOS"
}

actual fun getHomeDirectory(): String {
    val urls = NSFileManager.defaultManager.URLsForDirectory(directory = NSUserDirectory, inDomains = NSUserDomainMask)
    return urls[0].toString()
}

actual fun getCacheDirectory(): String {
    val urls = NSFileManager.defaultManager.URLsForDirectory(directory = NSCachesDirectory, inDomains = NSUserDomainMask)
    return urls[0].toString()
}

actual fun getImageCacheDirectory(): String {
    val urls = NSFileManager.defaultManager.URLsForDirectory(directory = NSCachesDirectory, inDomains = NSUserDomainMask)
    return urls[0].toString()
}

actual fun getDefaultRootDocumentDirectory(): String {
    val urls = NSFileManager.defaultManager.URLsForDirectory(directory = NSDocumentDirectory, inDomains = NSUserDomainMask)
    return urls[0].toString()
}

actual fun getConfigDirectory(): String {
//    val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(directory = NSDocumentDirectory, appropriateForURL = null ,inDomain = NSUserDomainMask, create = true, error = null)

    val urls = NSFileManager.defaultManager.URLsForDirectory(directory = NSDocumentDirectory, inDomains = NSUserDomainMask)

    val configpath = urls[0].
    if (!NSFileManager.defaultManager.fileExistsAtPath(configpath)) {
        NSFileManager.defaultManager.createDirectoryAtPath(configpath, null)
    }
//    if !FileManager.default.fileExists(atPath: path.absoluteString) {
//        try! FileManager.default.createDirectory(at: path, withIntermediateDirectories: true, attributes: nil)
//        }

    return urls[0].toString()
//    Napier.d(url?.absoluteString ?: "nothing found!")
//    Napier.d(urls.joinToString())
//    throw IllegalStateException(url?.absoluteString ?: "nothing found!")
//    throw IllegalStateException(url!!.path ?: "nothing found!")


//    return (url!!.path)!!
//    return (url!!.absoluteString)!!
}

fun initializeKoin() {
	startKoin {
		logger(NapierLogger())
		modules(mainModule)
	}
}

actual fun getPlatformFilesystem() = FileSystem.SYSTEM

actual val platformDefaultDispatcher: CoroutineContext = Dispatchers.Default
actual val platformIoDispatcher: CoroutineContext = Dispatchers.Default
actual val platformMainDispatcher: CoroutineContext = Dispatchers.Main