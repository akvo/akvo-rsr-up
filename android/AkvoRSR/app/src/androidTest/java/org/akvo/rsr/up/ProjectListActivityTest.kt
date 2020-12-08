package org.akvo.rsr.up

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectListActivityTest {

    @get:Rule
    val rule = activityScenarioRule<ProjectListActivity>()

    @Test
    fun activityShouldDisplayCorrectTitle() {
        ScreenRobot.withRobot(ProjectListRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkTitleIs(R.string.title_activity_project_list)
    }
    @Test
    fun activityShouldDisplayCorrectEmptyScreen() {
        ScreenRobot.withRobot(ProjectListRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkViewDisplayedWithText(R.id.projcountlabel, "0")
            .checkViewDisplayedWithTextId(R.id.unemployed_text, R.string.label_unemployed_proj_list)
    }

    class ProjectListRobot: ScreenRobot<ProjectListRobot>()
}
