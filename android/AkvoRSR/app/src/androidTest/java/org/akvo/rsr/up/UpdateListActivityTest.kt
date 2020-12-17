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
class UpdateListActivityTest {

    private val intent = Intent(
        ApplicationProvider.getApplicationContext(),
        UpdateListActivity::class.java
    )
        .putExtra(ConstantUtil.PROJECT_ID_KEY, "0")

    @get:Rule
    val rule = activityScenarioRule<UpdateListActivity>(intent)

    @Test
    fun activityShouldDisplayCorrectTitle() {
        ScreenRobot.withRobot(UpdateListRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkTitleIs(R.string.title_activity_update_list)
    }

    class UpdateListRobot : ScreenRobot<UpdateListRobot>()
}
