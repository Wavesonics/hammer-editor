package com.darkrockstudios.apps.hammer.e2e

import com.darkrockstudios.apps.hammer.base.http.ClientEntityState
import com.darkrockstudios.apps.hammer.base.http.EntityHash
import com.darkrockstudios.apps.hammer.base.http.HAMMER_PROTOCOL_HEADER
import com.darkrockstudios.apps.hammer.base.http.HAMMER_PROTOCOL_VERSION
import com.darkrockstudios.apps.hammer.base.http.HEADER_SYNC_ID
import com.darkrockstudios.apps.hammer.base.http.ProjectSynchronizationBegan
import com.darkrockstudios.apps.hammer.base.http.createJsonSerializer
import com.darkrockstudios.apps.hammer.base.http.synchronizer.EntityHasher
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.createAuthToken
import com.darkrockstudios.apps.hammer.e2e.util.EndToEndTest
import com.darkrockstudios.apps.hammer.e2e.util.TestDataSet1
import com.darkrockstudios.apps.hammer.utilities.hashEntity
import com.darkrockstudios.apps.hammer.utils.SERVER_EMPTY_NO_WHITELIST
import com.darkrockstudios.apps.hammer.utils.createTestServer
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.utils.io.core.toByteArray
import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectSyncTest : EndToEndTest() {

	lateinit var json: Json

	@Before
	override fun setup() {
		super.setup()

		json = createJsonSerializer()
	}

	@Test
	fun `A full ProjectSync where all entities are already up to date`(): Unit = runBlocking {
		val database = database()
		createTestServer(SERVER_EMPTY_NO_WHITELIST, fileSystem, database)
		TestDataSet1.createFullDataset(database)
		val userId = 1L
		val authToken = createAuthToken(userId, "test-install-id", database = database)
		doStartServer()

		client().apply {
			// Begin Sync
			val beginSyncResponse =
				post(api("project/$userId/${TestDataSet1.project1.name}/begin_sync")) {
					headers {
						append(HAMMER_PROTOCOL_HEADER, HAMMER_PROTOCOL_VERSION.toString())
						append("Authorization", "Bearer ${authToken.auth}")
					}
					contentType(ContentType.Application.OctetStream)
					parameter("projectId", TestDataSet1.project1.uuid.toString())

					val state = ClientEntityState(
						entities = TestDataSet1.user1Project1Entities.map { entity ->
							EntityHash(
								id = entity.id,
								hash = EntityHasher.hashEntity(entity)
							)
						}.toSet()
					)
					val stateJson = json.encodeToString(ClientEntityState.serializer(), state)
					val compressed = stateJson.toByteArray().compress(GZIP)

					setBody(compressed)
				}

			assertEquals(HttpStatusCode.OK, beginSyncResponse.status)
			val synchronizationBegan = beginSyncResponse.body<ProjectSynchronizationBegan>()

			println(synchronizationBegan)
			assertEquals(emptyList(), synchronizationBegan.idSequence)
			assertEquals(setOf(7), synchronizationBegan.deletedIds)

			// End Sync
			val endSyncResponse =
				get(api("project/$userId/${TestDataSet1.project1.name}/end_sync")) {
					headers {
						append(HAMMER_PROTOCOL_HEADER, HAMMER_PROTOCOL_VERSION.toString())
						append("Authorization", "Bearer ${authToken.auth}")
						append(HEADER_SYNC_ID, synchronizationBegan.syncId)
					}
					contentType(ContentType.Application.FormUrlEncoded)
					parameter("projectId", TestDataSet1.project1.uuid.toString())

					setBody(
						FormDataContent(
							Parameters.build {
								append("lastSync", synchronizationBegan.lastSync.toString())
								append("lastId", synchronizationBegan.lastId.toString())
							}
						)
					)
				}
			assertEquals(HttpStatusCode.OK, endSyncResponse.status)
		}
	}
}