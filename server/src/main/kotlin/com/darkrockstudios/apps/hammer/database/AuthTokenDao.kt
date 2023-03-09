package com.darkrockstudios.apps.hammer.database

import com.darkrockstudios.apps.hammer.AuthToken
import com.darkrockstudios.apps.hammer.account.Token
import com.darkrockstudios.apps.hammer.utilities.toISO8601
import kotlinx.datetime.Instant
import java.util.UUID

class AuthTokenDao(database: Database) {
    private val queries = database.serverDatabase.authTokenQueries

    suspend fun getTokensByEmail(email: String): List<AuthToken> {
        val query = queries.getTokensByEmail(email)
        return query.executeAsList()
    }

    suspend fun getTokenByToken(token: Token): AuthToken? {
        val query = queries.getTokenByToken(token.value)
        return query.executeAsOneOrNull()
    }

    suspend fun setToken(email: String, deviceId: String, newToken: Token, expires: Instant) {
        val expiresString = expires.toISO8601()
        queries.setToken(email = email, deviceId = deviceId, token = newToken.value, expires = expiresString)
    }

    suspend fun getTokenByDeviceId(deviceId: String): AuthToken? {
        val query = queries.getTokenByDeviceId(deviceId)
        return query.executeAsOneOrNull()
    }
}