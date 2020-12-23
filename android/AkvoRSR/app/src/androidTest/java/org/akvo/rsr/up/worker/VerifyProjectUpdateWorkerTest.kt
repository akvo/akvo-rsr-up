package org.akvo.rsr.up.worker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import org.akvo.rsr.up.dao.RsrDbAdapter
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.SettingsUtil
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class VerifyProjectUpdateWorkerTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var wmRule = WorkManagerTestRule()

    @Test
    fun testSubmitRepeatedEnqueued() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        SettingsUtil.signOut(targetContext)
        val user = createFakeTestUser()
        SettingsUtil.signIn(targetContext, user)
        Assert.assertTrue(SettingsUtil.haveCredentials(targetContext))

        val request = PeriodicWorkRequestBuilder<VerifyProjectUpdateWorker>(15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES).build()
        wmRule.workManager.enqueue(request).result.get()
        wmRule.testDriver.setInitialDelayMet(request.id)
        wmRule.testDriver.setPeriodDelayMet(request.id)
        wmRule.testDriver.setAllConstraintsMet(request.id)

        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, CoreMatchers.`is`(WorkInfo.State.ENQUEUED))
    }

    @Test
    fun testSubmitOnceFailed() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        SettingsUtil.signOut(targetContext)
        val user = createFakeTestUser()
        SettingsUtil.signIn(targetContext, user)
        Assert.assertTrue(SettingsUtil.haveCredentials(targetContext))
        val dba = RsrDbAdapter(targetContext)
        dba.open()
        val update = createTestUpdate()
        update.id = SettingsUtil.ReadInt(targetContext, ConstantUtil.LOCAL_ID_KEY, -1).toString()
        dba.saveUpdate(update, false)

        val request = OneTimeWorkRequestBuilder<VerifyProjectUpdateWorker>().build()
        wmRule.workManager.enqueue(request).result.get()

        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, CoreMatchers.`is`(WorkInfo.State.FAILED))
        dba.clearAllData()
        dba.close()
    }
}
