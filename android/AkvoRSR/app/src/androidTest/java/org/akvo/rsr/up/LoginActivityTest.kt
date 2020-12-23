package org.akvo.rsr.up

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.akvo.rsr.up.util.SettingsUtil
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    lateinit var scenario: ActivityScenario<LoginActivity>

    @Test
    fun activityShouldBeDisplayedCorrectly() {
        SettingsUtil.signOut(InstrumentationRegistry.getInstrumentation().targetContext)
        assertFalse(SettingsUtil.haveCredentials(InstrumentationRegistry.getInstrumentation().targetContext))

        val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        scenario = launchActivity(intent)

        ScreenRobot.withRobot(LoginScreenRobot::class.java).checkViewDisplayedWithId(R.id.btn_login)
    }

    class LoginScreenRobot : ScreenRobot<LoginScreenRobot>()
}
