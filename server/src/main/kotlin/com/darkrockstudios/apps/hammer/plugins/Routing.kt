package com.darkrockstudios.apps.hammer.plugins

import com.darkrockstudios.apps.hammer.account.accountRoutes
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    routing {
        get("/teapot") {
            call.respondText("I'm a little ", status = HttpStatusCode.fromValue(418))
        }
    }

    accountRoutes()
}
