package org.akvo.rsr.up

import android.preference.Preference
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.PreferenceMatchers.withTitle
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    @get:Rule
    val rule = activityScenarioRule<SettingsActivity>()

    @Test
    fun activityShouldDisplayCorrectPreference() {
        ScreenRobot.withRobot(SettingsScreenRobot::class.java)
            .checkPreferenceDisplayedWithTitle(R.string.label_setting_delaypics)
    }

    class SettingsScreenRobot : ScreenRobot<SettingsScreenRobot>() {

        fun checkPreferenceDisplayedWithTitle(@StringRes title: Int): SettingsScreenRobot {
            onData(
                allOf(
                    `is`(
                        instanceOf(Preference::class.java)
                    ),
                    withTitle(title)
                )
            )
                .check(matches(isDisplayed()))
            return this
        }
    }
}
