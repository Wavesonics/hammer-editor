package com.darkrockstudios.apps.hammer.common.globalsettings

import com.darkrockstudios.apps.hammer.common.dependencyinjection.createApi
import com.darkrockstudios.apps.hammer.common.dependencyinjection.createKtorfit
import com.darkrockstudios.apps.hammer.common.dependencyinjection.injectDefaultDispatcher
import com.darkrockstudios.apps.hammer.common.dependencyinjection.injectMainDispatcher
import com.darkrockstudios.apps.hammer.common.server.ServerAccountApi
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HttpManager(serverSettingsUpdates: SharedFlow<ServerSettings?>) : KoinComponent {
    private val dispatcherMain by injectMainDispatcher()
    private val dispatcherDefault by injectDefaultDispatcher()
    private val scope = CoroutineScope(dispatcherDefault)

    private val httpClient: HttpClient by inject()
    private var ktorfit: Ktorfit? = null

    var accountApi: ServerAccountApi? = null

    /*
    // TODO don't do this automatically?
    init {
        scope.launch {
            serverSettingsUpdates.collect { settings ->
                onSettingsUpdate(settings)
            }
        }
    }
    */

    fun onSettingsUpdate(settings: ServerSettings?) {
        if (settings != null) {
            val newKtorfit = createKtorfit(httpClient, settings.url)
            ktorfit = newKtorfit
            accountApi = createApi<ServerAccountApi>(newKtorfit)
        } else {
            ktorfit = null
        }
    }
}