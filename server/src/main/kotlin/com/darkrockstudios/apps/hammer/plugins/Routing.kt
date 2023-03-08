package com.darkrockstudios.apps.hammer.plugins

import com.darkrockstudios.apps.hammer.account.testRoutes
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }

    testRoutes()
}
