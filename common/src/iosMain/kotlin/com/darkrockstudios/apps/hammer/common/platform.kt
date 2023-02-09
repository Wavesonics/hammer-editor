package com.darkrockstudios.apps.hammer.common

import com.darkrockstudios.apps.hammer.common.dependencyinjection.NapierLogger
import com.darkrockstudios.apps.hammer.common.dependencyinjection.mainModule
import kotlinx.coroutines.Dispatchers
import okio.FileSystem
import org.koin.core.context.startKoin
import kotlin.coroutines.CoroutineContext

actual fun getPlatformName(): String {
    return "iOS"
}

actual fun getHomeDirectory(): String {
    return "not implemented"
}

actual fun getCacheDirectory(): String {
    return "not implemented"
}

actual fun getImageCacheDirectory(): String {
    return "not implemented"
}

actual fun getDefaultRootDocumentDirectory(): String {
	//let path = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent(“todos.txt”)
	return "not implemented"
}

actual fun getConfigDirectory(): String = "not implemented"

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