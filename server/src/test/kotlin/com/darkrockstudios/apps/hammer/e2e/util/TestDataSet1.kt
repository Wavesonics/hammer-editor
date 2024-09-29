package com.darkrockstudios.apps.hammer.e2e.util

import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.addDeletedProject
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.createAccount
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.createProject
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.createTestNote
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.createTestScene
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.insertDeletedEntity
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.insertEntity
import com.darkrockstudios.apps.hammer.e2e.util.E2eTestData.preDeletedProject1
import korlibs.io.util.UUID

object TestDataSet1 {
	val account1 = TestAccount(
		email = "test@test.com",
		password = "password123!@#",
		salt = "fake-salt",
	)
	val project1 = TestProject(
		name = "test-project",
		uuid = UUID.randomUUID(),
		userId = 1,
	)

	fun createFullDataset(database: SqliteTestDatabase) {
		createAccount(account1, database)
		createProject(project1, database)

		addDeletedProject(1, preDeletedProject1, database)

		(1..5).forEach { ii ->
			insertEntity(
				userId = 1,
				projectId = 1,
				entity = createTestScene(ii),
				testDatabase = database,
			)
		}
		insertEntity(
			userId = 1,
			projectId = 1,
			entity = createTestNote(6),
			testDatabase = database,
		)
		insertDeletedEntity(
			id = 7,
			userId = 1,
			projectId = 1,
			testDatabase = database,
		)
		insertEntity(
			userId = 1,
			projectId = 1,
			entity = createTestNote(8),
			testDatabase = database,
		)
	}
}