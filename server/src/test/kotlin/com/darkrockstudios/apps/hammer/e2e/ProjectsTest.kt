package com.darkrockstudios.apps.hammer.e2e

import com.darkrockstudios.apps.hammer.base.http.BeginProjectsSyncResponse
import com.darkrockstudios.apps.hammer.base.http.HAMMER_PROTOCOL_HEADER
import com.darkrockstudios.apps.hammer.base.http.HAMMER_PROTOCOL_VERSION
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.createAuthToken
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.preDeletedProject1
import com.darkrockstudios.apps.hammer.e2e.util.EndToEndTest
import com.darkrockstudios.apps.hammer.e2e.util.TestDataSet1
import com.darkrockstudios.apps.hammer.utils.SERVER_EMPTY_NO_WHITELIST
import com.darkrockstudios.apps.hammer.utils.createTestServer
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

	@Test
	fun `Project - Begin Sync - Success`(): Unit = runBlocking {
		val database = database()
		createTestServer(SERVER_EMPTY_NO_WHITELIST, fileSystem, database)
		TestDataSet1.createFullDataset(database)
		val userId = 1L
		val authToken = createAuthToken(userId, "test-install-id", database = database)
		doStartServer()

		client().apply {
			val response = get(api("projects/$userId/begin_sync")) {
				headers {
					append(HAMMER_PROTOCOL_HEADER, HAMMER_PROTOCOL_VERSION.toString())
					append("Authorization", "Bearer ${authToken.auth}")
				}
			}

			assertEquals(HttpStatusCode.OK, response.status)

			val beginSyncResponse: BeginProjectsSyncResponse = response.body()
			assertTrue(beginSyncResponse.syncId.isNotEmpty())
			assertEquals(1, beginSyncResponse.projects.size)
			assertEquals(1, beginSyncResponse.deletedProjects.size)

			beginSyncResponse.projects.first().let { project ->
				assertEquals(TestDataSet1.project1.uuid.toString(), project.uuid.id)
				assertEquals(TestDataSet1.project1.name, project.name)
			}

			beginSyncResponse.deletedProjects.first().let { uuid ->
				assertEquals(preDeletedProject1.toString(), uuid.id)
			}
		}
	}
}