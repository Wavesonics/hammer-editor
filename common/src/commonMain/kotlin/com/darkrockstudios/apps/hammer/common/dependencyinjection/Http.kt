package com.darkrockstudios.apps.hammer.common.dependencyinjection

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*

fun createHttpClient(): HttpClient {
    val client = HttpClient(getHttpPlatformEngine()) {
        install(Logging) {
            logger = NapierHttpLogger()
            level = LogLevel.HEADERS
        }

        install(ContentNegotiation) {
            json()
        }

        install(Auth) {
            bearer {
                loadTokens {
                    // TODO: Load tokens from a local storage and return them as the 'BearerTokens' instance
                    BearerTokens("abc123", "xyz111")
                }
                //refreshTokens {

                //}
            }
        }
    }

    return client
}

fun createKtorfit(client: HttpClient, baseUrl: String): Ktorfit {
    //.baseUrl("https://swapi.dev/api/")
    return Ktorfit.Builder().httpClient(client).baseUrl(baseUrl).build()
}

fun <T> createApi(ktorfit: Ktorfit): T = ktorfit.create()

private class NapierHttpLogger : Logger {
    override fun log(message: String) {
        Napier.i(tag = "Http", message = message)
    }
}

expect fun getHttpPlatformEngine(): HttpClientEngineFactory<*>