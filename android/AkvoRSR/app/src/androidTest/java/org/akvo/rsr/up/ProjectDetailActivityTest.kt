package org.akvo.rsr.up

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.akvo.rsr.up.util.ConstantUtil
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectDetailActivityTest {

    private val intent = Intent(ApplicationProvider.getApplicationContext(),
        ProjectDetailActivity::class.java)
        .putExtra(ConstantUtil.PROJECT_ID_KEY, "0")

    @get:Rule
    val rule = activityScenarioRule<ProjectDetailActivity>(intent)

    @Test
    fun activityShouldDisplayCorrectTitle() {
        ScreenRobot.withRobot(ProjectDetailRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkTitleIs(R.string.title_activity_project_detail)
    }

    class ProjectDetailRobot: ScreenRobot<ProjectDetailRobot>()
}
