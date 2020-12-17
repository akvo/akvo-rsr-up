package org.akvo.rsr.up

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.akvo.rsr.up.dao.RsrDbAdapter
import org.akvo.rsr.up.util.ConstantUtil
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateEditorActivityTest {

    lateinit var scenario: ActivityScenario<UpdateEditorActivity>

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule
        .grant(Manifest.permission.ACCESS_FINE_LOCATION)

    @Test
    fun activityShouldDisplayErrorMessageForNullUpdateAndProject() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            UpdateEditorActivity::class.java
        )
        scenario = launchActivity(intent)

        ScreenRobot.withRobot(UpdateEditorRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkErrorMissingUpdate()
    }

    @Test
    fun activityShouldShowCorrectTitle() {
        val dba = RsrDbAdapter(ApplicationProvider.getApplicationContext())
        dba.open()
        dba.createProject("test")

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            UpdateEditorActivity::class.java
        ).putExtra(ConstantUtil.PROJECT_ID_KEY, "0")

        scenario = launchActivity(intent)

        ScreenRobot.withRobot(UpdateEditorRobot::class.java)
            .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
            .checkTitleIs(R.string.title_activity_update_edit)

        dba.deleteAllProjects()
        dba.close()
    }

    @Test
    fun activityShouldRequestLocationWhenGpsButtonPress() {
        val dba = RsrDbAdapter(ApplicationProvider.getApplicationContext())
        dba.open()
        dba.createProject("test")

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            UpdateEditorActivity::class.java
        ).putExtra(ConstantUtil.PROJECT_ID_KEY, "0")

        scenario = launchActivity(intent)

        val locMgr = InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ScreenRobot.withRobot(UpdateEditorRobot::class.java)
                .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
                .clickOnViewWithId(R.id.btn_gps_position)
                .checkViewDisplayedWithTextId(R.id.btn_gps_position, R.string.btncaption_gps_cancel)
        } else {
            ScreenRobot.withRobot(UpdateEditorRobot::class.java)
                .provideContext(InstrumentationRegistry.getInstrumentation().targetContext)
                .clickOnViewWithId(R.id.btn_gps_position)
                .checkErrorEnableLocation()
        }

        dba.deleteAllProjects()
        dba.close()
    }

    class UpdateEditorRobot : ScreenRobot<UpdateEditorRobot>() {

        fun checkErrorMissingUpdate(): UpdateEditorRobot {
            return checkViewWithTextIsDisplayed(R.string.noproj_dialog_title)
                .checkViewWithTextIsDisplayed(R.string.noproj_dialog_msg)
        }

        fun checkErrorEnableLocation(): UpdateEditorRobot {
            return checkViewWithTextIsDisplayed(R.string.gpsdisabled_dialog_msg)
        }
    }
}
