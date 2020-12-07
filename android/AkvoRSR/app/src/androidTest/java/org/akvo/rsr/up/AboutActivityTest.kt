package org.akvo.rsr.up

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.akvo.rsr.up.ScreenRobot.Companion.withRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AboutActivityTest {

    @get:Rule
    val rule = activityScenarioRule<AboutActivity>()

    @Test
    fun activityShouldDisplayCorrectTitle() {
        withRobot(AboutScreenRobot::class.java).provideContext(getInstrumentation().targetContext)
            .checkTitleIs(R.string.title_activity_about)
    }

    class AboutScreenRobot : ScreenRobot<AboutScreenRobot>()
}
