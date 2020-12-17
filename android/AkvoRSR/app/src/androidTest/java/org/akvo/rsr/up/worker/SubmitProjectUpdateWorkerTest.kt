package org.akvo.rsr.up.worker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.workDataOf
import org.akvo.rsr.up.BuildConfig
import org.akvo.rsr.up.dao.RsrDbAdapter
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.SettingsUtil
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

class SubmitProjectUpdateWorkerTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var wmRule = WorkManagerTestRule()

    @Test
    fun testSubmitProjectWorkerSuccess() {
        login()

        val update = createTestUpdate()

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        var nextLocalId = SettingsUtil.ReadInt(targetContext, ConstantUtil.LOCAL_ID_KEY, -1)
        val dba = RsrDbAdapter(targetContext)
        dba.open()
        update.id = nextLocalId.toString()
        nextLocalId--
        SettingsUtil.WriteInt(
            targetContext,
            ConstantUtil.LOCAL_ID_KEY,
            nextLocalId
        )
        dba.saveUpdate(update, true)

        val inputData = workDataOf(ConstantUtil.UPDATE_ID_KEY to update.id)

        val request = OneTimeWorkRequestBuilder<SubmitProjectUpdateWorker>()
            .setInputData(inputData)
            .build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))

        dba.clearAllData()
        dba.close()
    }

    private fun login() {
        SettingsUtil.signOut(InstrumentationRegistry.getInstrumentation().targetContext)

        val inputData = workDataOf(
            ConstantUtil.USERNAME_KEY to BuildConfig.TEST_USER,
            ConstantUtil.PASSWORD_KEY to BuildConfig.TEST_PASSWORD
        )

        val request = OneTimeWorkRequestBuilder<SignInWorker>()
            .setInputData(inputData)
            .build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
        Assert.assertTrue(SettingsUtil.haveCredentials(InstrumentationRegistry.getInstrumentation().targetContext))
    }

    @Test
    fun testSubmitProjectWorkerFailure() {
        // input data will be null
        val request = OneTimeWorkRequestBuilder<SubmitProjectUpdateWorker>().build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }
}
