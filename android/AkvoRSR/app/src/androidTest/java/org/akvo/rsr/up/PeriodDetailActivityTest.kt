package org.akvo.rsr.up

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class PeriodDetailActivityTest {

    @get:Rule
    val rule = activityScenarioRule<PeriodDetailActivity>()

    @Test
    fun activityShouldDisplayCorrectTitle() {
        ScreenRobot.withRobot(PeriodDetailRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkErrorMissingUpdate()
    }

    class PeriodDetailRobot : ScreenRobot<PeriodDetailRobot>() {

        fun checkErrorMissingUpdate(): PeriodDetailRobot {
            return checkViewWithTextIsDisplayed(R.string.noupd_dialog_title)
                .checkViewWithTextIsDisplayed(R.string.noupd_dialog_msg)
        }
    }
}
