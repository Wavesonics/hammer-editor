package com.darkrockstudios.apps.hammer.account

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.testRoutes() {

    routing {
        test()
        jsonTest()
        authenticate("BasicAuth") {
            testAuth()
        }
    }
}

private fun Route.test() {
    get("/test") {
        call.respondText("Hello, world!")
    }
}

private fun Route.testAuth() {
    get("/test_auth") {
        val principal = call.principal<UserIdPrincipal>()!!
        call.respondText("Authenticated as ${principal.name}")
    }
}

private fun Route.jsonTest() {
    get("/test_json") {
        call.respond(mapOf("hello" to "world"))
    }
}