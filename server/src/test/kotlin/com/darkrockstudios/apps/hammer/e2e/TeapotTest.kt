package com.darkrockstudios.apps.hammer.e2e

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContains

class TeapotTest : E2eTest() {
	@Test
	fun `Teapot Endpoint`(): Unit = runBlocking {
		client().apply {
			val response: String = get(api("teapot")).body()
			assertContains(response, "I'm a little Tea Pot")
		}
	}
}