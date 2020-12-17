package org.akvo.rsr.up.worker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.SettingsUtil
import org.hamcrest.CoreMatchers
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SubmitEmploymentWorkerTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var wmRule = WorkManagerTestRule()

    @Test
    fun testSubmitEmploymentFailure() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        SettingsUtil.signOut(targetContext)
        val user = createFakeTestUser()
        SettingsUtil.signIn(targetContext, user)
        assertTrue(SettingsUtil.haveCredentials(targetContext))

        val request = OneTimeWorkRequestBuilder<SubmitEmploymentWorker>().build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, CoreMatchers.`is`(WorkInfo.State.FAILED))
        assertTrue(workInfo.outputData.getString(ConstantUtil.SERVICE_ERRMSG_KEY)!!
            .contains("Server rejected employment request"))
    }
}
