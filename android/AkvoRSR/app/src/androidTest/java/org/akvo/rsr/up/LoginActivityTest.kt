package org.akvo.rsr.up

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val rule = activityScenarioRule<LoginActivity>()

    @Test
    fun activityShouldBeDisplayedCorrectly() {
        ScreenRobot.withRobot(LoginScreenRobot::class.java).checkViewDisplayedWithId(R.id.btn_login)
    }

    class LoginScreenRobot : ScreenRobot<LoginScreenRobot>()
}
