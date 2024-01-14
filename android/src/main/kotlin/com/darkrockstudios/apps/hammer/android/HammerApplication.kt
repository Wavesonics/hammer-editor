package com.darkrockstudios.apps.hammer.android

import android.app.Application
import com.darkrockstudios.apps.hammer.android.aboutlibraries.aboutLibrariesModule
import com.darkrockstudios.apps.hammer.common.data.migrator.DataMigrator
import com.darkrockstudios.apps.hammer.common.dependencyinjection.NapierLogger
import com.darkrockstudios.apps.hammer.common.dependencyinjection.appModule
import com.darkrockstudios.apps.hammer.common.dependencyinjection.imageLoadingModule
import com.darkrockstudios.apps.hammer.common.dependencyinjection.mainModule
import com.darkrockstudios.apps.hammer.common.setDirectories
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent

class HammerApplication : Application() {

	private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	override fun onCreate() {
		super.onCreate()

		setDirectories(this)

		Napier.base(DebugAntilog())

		startKoin {
			logger(NapierLogger())
			androidContext(this@HammerApplication)
			modules(mainModule, imageLoadingModule, aboutLibrariesModule, appModule(applicationScope))
		}

		KoinJavaComponent.getKoin().get<DataMigrator>(DataMigrator::class).handleDataMigration()
	}

	override fun onTerminate() {
		super.onTerminate()
		applicationScope.cancel("Application onTerminate")
	}
}