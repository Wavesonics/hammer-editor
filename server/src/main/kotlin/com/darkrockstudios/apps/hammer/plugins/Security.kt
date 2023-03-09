package com.darkrockstudios.apps.hammer.plugins

import com.darkrockstudios.apps.hammer.account.AccountsRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.koin.ktor.ext.inject

const val USER_AUTH_NAME = "UserAuth"
const val AUTH_REAL = "User Resources"

fun Application.configureSecurity() {
    val accountRepo: AccountsRepository by inject()

    authentication {
        bearer(
            name = USER_AUTH_NAME,
        ) {
            realm = AUTH_REAL
            authenticate { tokenCredential ->
                val result = accountRepo.checkToken(tokenCredential.token)
                if (result.isSuccess) {
                    val userEmail = result.getOrThrow()
                    UserIdPrincipal(userEmail)

                } else {
                    null
                }
            }
        }
    }
}
