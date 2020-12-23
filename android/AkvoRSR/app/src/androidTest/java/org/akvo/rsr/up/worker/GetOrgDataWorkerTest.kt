package org.akvo.rsr.up.worker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import org.akvo.rsr.up.util.SettingsUtil
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class GetOrgDataWorkerTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var wmRule = WorkManagerTestRule()

    @Test
    fun testGetOrgDataFailure() {
        SettingsUtil.signOut(InstrumentationRegistry.getInstrumentation().targetContext)

        val request = OneTimeWorkRequestBuilder<GetOrgDataWorker>().build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        Assert.assertThat(workInfo.state, CoreMatchers.`is`(WorkInfo.State.FAILED))
    }
}
