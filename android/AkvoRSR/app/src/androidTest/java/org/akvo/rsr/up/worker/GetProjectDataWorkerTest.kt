package org.akvo.rsr.up.worker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import org.akvo.rsr.up.dao.RsrDbAdapter
import org.akvo.rsr.up.domain.Country
import org.akvo.rsr.up.util.SettingsUtil
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GetProjectDataWorkerTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var wmRule = WorkManagerTestRule()

    @Test
    fun testGetProjectDataWorkerFailureIfNoUser() {
        SettingsUtil.signOut(InstrumentationRegistry.getInstrumentation().targetContext)

        val request = OneTimeWorkRequestBuilder<GetProjectDataWorker>().build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }

    @Test
    fun testGetProjectDataWorkerFailureIfWrongUser() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        SettingsUtil.signOut(targetContext)
        val user = createFakeTestUser()
        SettingsUtil.signIn(targetContext, user)
        assertTrue(SettingsUtil.haveCredentials(targetContext))

        val request = OneTimeWorkRequestBuilder<GetProjectDataWorker>().build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
        SettingsUtil.signOut(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun testGetProjectDataWorkerIfUserHasNoProjects() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        SettingsUtil.signOut(targetContext)

        val user = createExistingTestUser()
        SettingsUtil.signIn(targetContext, user)
        assertTrue(SettingsUtil.haveCredentials(targetContext))
        SettingsUtil.WriteBoolean(targetContext, "setting_fullsynch", false)
        val country = Country()
        country.id = "12"
        country.name = "Spain"
        country.continent = "Europe"
        country.isoCode = "ES"
        val dba = RsrDbAdapter(targetContext)
        dba.open()
        dba.saveCountry(country);

        val request = OneTimeWorkRequestBuilder<GetProjectDataWorker>().build()

        wmRule.workManager.enqueue(request).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))

        dba.clearAllData()
        dba.close()
        SettingsUtil.signOut(InstrumentationRegistry.getInstrumentation().targetContext)
    }

}
