package com.darkrockstudios.apps.hammer.common.dependencyinjection

import io.ktor.client.engine.*
import io.ktor.client.engine.jetty.*

actual fun getHttpPlatformEngine(): HttpClientEngineFactory<*> = Jetty