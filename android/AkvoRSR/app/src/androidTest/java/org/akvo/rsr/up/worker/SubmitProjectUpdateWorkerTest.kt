package org.akvo.rsr.up.worker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.workDataOf
import org.akvo.rsr.up.dao.RsrDbAdapter
import org.akvo.rsr.up.domain.Update
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.SettingsUtil
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.UUID

class SubmitProjectUpdateWorkerTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var wmRule = WorkManagerTestRule()

    @Test
    fun testSignInWorkSuccess() {
        val update = Update()
        update.uuid = UUID.randomUUID().toString()
        update.userId = "45994"
        update.date = Date()
        update.unsent = true
        update.draft = false
        update.projectId = "2"
        update.title = "Some title ${update.uuid}"
        update.text = "Some description ${update.uuid}"

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        var nextLocalId = SettingsUtil.ReadInt(targetContext, ConstantUtil.LOCAL_ID_KEY, -1)
        val dba = RsrDbAdapter(targetContext)
        dba.open()
        update.id = nextLocalId.toString()
        nextLocalId--
        SettingsUtil.WriteInt(targetContext,
            ConstantUtil.LOCAL_ID_KEY,
            nextLocalId)
        dba.saveUpdate(update, true)
        dba.close()

        val inputData = workDataOf(ConstantUtil.UPDATE_ID_KEY to update.id)

        val request = OneTimeWorkRequestBuilder<SubmitProjectUpdateWorker>()
            .setInputData(inputData)
            .build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))

        //TODO: delete update
    }

    @Test
    fun testSignInWorkFailure() {
        //input data will be null
        val request = OneTimeWorkRequestBuilder<SubmitProjectUpdateWorker>().build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }
}