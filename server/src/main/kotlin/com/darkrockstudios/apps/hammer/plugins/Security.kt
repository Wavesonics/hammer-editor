package com.darkrockstudios.apps.hammer.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureSecurity() {
	authentication {
		basic(name = "BasicAuth") {
			realm = "Ktor Server"
			validate { credentials ->
				if (credentials.name == "adam" && credentials.password == "test") {
					UserIdPrincipal(credentials.name)
				} else {
					null
				}
			}
		}
	}
}
