package org.akvo.rsr.up.worker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.workDataOf
import org.akvo.rsr.up.BuildConfig
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.SettingsUtil
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignInWorkerTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var wmRule = WorkManagerTestRule()

    @Test
    fun testSignInWorkSuccess() {

        val inputData = workDataOf(ConstantUtil.USERNAME_KEY to BuildConfig.TEST_USER,
            ConstantUtil.PASSWORD_KEY to BuildConfig.TEST_PASSWORD)

        val request = OneTimeWorkRequestBuilder<SignInWorker>()
            .setInputData(inputData)
            .build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
        assertTrue(SettingsUtil.haveCredentials(InstrumentationRegistry.getInstrumentation().targetContext))
    }

    @Test
    fun testSignInWorkFailure() {
        //input data will be null
        val request = OneTimeWorkRequestBuilder<SignInWorker>().build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }
}