package com.darkrockstudios.apps.hammer.common.server

import de.jensklingenberg.ktorfit.http.*

interface ServerAccountApi {
    @POST("account/create_account/")
    @FormUrlEncoded
    suspend fun createAccount(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("deviceId") deviceId: String,
    ): String

    @POST("account/login/")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("deviceId") deviceId: String,
    ): String

    @GET("account/test_auth/")
    suspend fun testAuth(): String
}