package org.akvo.rsr.up

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateEditorActivityTest {

    @get:Rule
    val rule = activityScenarioRule<UpdateEditorActivity>()

    @Test
    fun activityShouldDisplayErrorMessageForNullUpdateAndProject() {
        ScreenRobot.withRobot(UpdateEditorRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkErrorMissingUpdate()
    }

    class UpdateEditorRobot : ScreenRobot<UpdateEditorRobot>() {

        fun checkErrorMissingUpdate(): UpdateEditorRobot {
            return checkViewWithTextIsDisplayed(R.string.noproj_dialog_title)
                .checkViewWithTextIsDisplayed(R.string.noproj_dialog_msg)
        }
    }
}
