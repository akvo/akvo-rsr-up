package org.akvo.rsr.up.worker

import org.akvo.rsr.up.BuildConfig
import org.akvo.rsr.up.domain.Update
import org.akvo.rsr.up.domain.User
import java.util.Date
import java.util.UUID

fun createTestUpdate(): Update {
    val update = Update()
    update.uuid = UUID.randomUUID().toString()
    update.userId = "45994"
    update.date = Date()
    update.unsent = true
    update.draft = false
    update.projectId = "2"
    update.title = "Some title ${update.uuid}"
    update.text = "Some description ${update.uuid}"
    return update
}

fun createExistingTestUser(): User {
    val user = User()
    user.username = BuildConfig.TEST_USER
    user.id = "45994"
    user.apiKey = "test123"
    return user
}


fun createFakeTestUser(): User {
    val user = User()
    user.username = "test"
    user.id = "123"
    user.apiKey = "test123"
    return user
}
