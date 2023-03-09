package com.darkrockstudios.apps.hammer.account

import com.darkrockstudios.apps.hammer.plugins.USER_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.accountRoutes() {
    routing {
        createAccount()
        login()
        authenticate(USER_AUTH_NAME) {
            testAuth()
        }
    }
}

private fun Route.createAccount() {
    val accountsRepository: AccountsRepository = get()

    post("/create_account") {
        val formParameters = call.receiveParameters()
        val email = formParameters["email"].toString()
        val password = formParameters["password"].toString()
        val deviceId = formParameters["deviceId"].toString()

        val result = accountsRepository.createAccount(email = email, deviceId = deviceId, password = password)
        if (result.isSuccess) {
            val token = result.getOrThrow().value
            call.respondText(token, status = HttpStatusCode.Created)
        } else {
            call.respondText("Failed to create account", status = HttpStatusCode.Conflict)
        }
    }
}

private fun Route.login() {
    val accountsRepository: AccountsRepository = get()

    post("/login") {
        val formParameters = call.receiveParameters()
        val email = formParameters["email"].toString()
        val password = formParameters["password"].toString()
        val deviceId = formParameters["deviceId"].toString()

        val result = accountsRepository.login(email = email, password = password, deviceId = deviceId)
        if (result.isSuccess) {
            val authToken = result.getOrThrow()
            call.respondText(authToken.value)
        } else {
            call.respondText("Failed to authenticate", status = HttpStatusCode.Unauthorized)
        }
    }
}

private fun Route.testAuth() {
    get("/test_auth") {
        val principal = call.principal<UserIdPrincipal>()!!
        call.respondText("Authenticated as ${principal.name}")
    }
}

/*
private fun Route.jsonTest() {
    get("/test_json") {
        call.respond(mapOf("hello" to "world"))
    }
}
*/