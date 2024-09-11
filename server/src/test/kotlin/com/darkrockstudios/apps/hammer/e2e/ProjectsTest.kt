package com.darkrockstudios.apps.hammer.e2e

import com.darkrockstudios.apps.hammer.base.http.HAMMER_PROTOCOL_HEADER
import com.darkrockstudios.apps.hammer.base.http.HAMMER_PROTOCOL_VERSION
import com.darkrockstudios.apps.hammer.e2e.util.EndToEndTest
import com.darkrockstudios.apps.hammer.utils.SERVER_EMPTY_NO_WHITELIST
import com.darkrockstudios.apps.hammer.utils.createTestServer
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectsTest : EndToEndTest() {
	@Test
	fun `Project - Begin Sync - No auth`(): Unit = runBlocking {
		createTestServer(SERVER_EMPTY_NO_WHITELIST, fileSystem, database())
		doStartServer()

		client().apply {
			val response = get(api("projects/1/begin_sync")) {
				headers {
					append(HAMMER_PROTOCOL_HEADER, HAMMER_PROTOCOL_VERSION.toString())
				}
			}

			assertEquals(HttpStatusCode.Unauthorized, response.status)
		}
	}

//	@Test
//	fun `Project - Begin Sync - `(): Unit = runBlocking {
//		createTestServer(SERVER_EMPTY_NO_WHITELIST, fileSystem, database())
//		doStartServer()
//
//		client().apply {
//			val response = get(api("projects/1/begin_sync"))
//
//			assertEquals(HttpStatusCode.Unauthorized, response.status)
//		}
//	}
}