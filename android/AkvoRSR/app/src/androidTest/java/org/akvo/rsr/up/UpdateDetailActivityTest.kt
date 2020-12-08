package org.akvo.rsr.up

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.akvo.rsr.up.util.ConstantUtil
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateDetailActivityTest {

    lateinit var scenario: ActivityScenario<UpdateDetailActivity>

    private val intent = Intent(ApplicationProvider.getApplicationContext(),
        UpdateDetailActivity::class.java).putExtra(ConstantUtil.PROJECT_ID_KEY, "0")
        .putExtra(ConstantUtil.UPDATE_ID_KEY, "0")

    @get:Rule
    val rule = activityScenarioRule<UpdateDetailActivity>(intent)

    @Test
    fun activityShouldDisplayErrorMessageForMissingUpdate() {
        val intent = Intent(ApplicationProvider.getApplicationContext(),
            UpdateDetailActivity::class.java).putExtra(ConstantUtil.PROJECT_ID_KEY, "0")
            .putExtra(ConstantUtil.UPDATE_ID_KEY, "0")
        scenario = launchActivity(intent)

        ScreenRobot.withRobot(UpdateDetailRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkErrorMissingUpdate()
    }

    @Test
    fun activityShouldDisplayErrorMessageForNullUpdateAndProject() {
        val intent = Intent(ApplicationProvider.getApplicationContext(),
            UpdateDetailActivity::class.java)
        scenario = launchActivity(intent)

        ScreenRobot.withRobot(UpdateDetailRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkErrorMissingUpdate()
    }

    @After
    fun cleanup() {
        scenario.close()
    }

    class UpdateDetailRobot : ScreenRobot<UpdateDetailRobot>() {

        fun checkErrorMissingUpdate(): UpdateDetailRobot {
            return checkViewWithTextIsDisplayed(R.string.noupd_dialog_title)
                .checkViewWithTextIsDisplayed(R.string.noupd_dialog_msg)
        }
    }
}
