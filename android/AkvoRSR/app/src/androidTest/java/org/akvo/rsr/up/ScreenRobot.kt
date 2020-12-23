package org.akvo.rsr.up

import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf

@Suppress("UNCHECKED_CAST")
abstract class ScreenRobot<T : ScreenRobot<T>> {

    private var context: Context? = null

    fun checkTitleIs(@StringRes stringRes: Int): T {
        onView(
            allOf(
                isAssignableFrom(AppCompatTextView::class.java),
                withParent(isAssignableFrom(Toolbar::class.java))
            )
        )
            .check(matches(withText((context!!.getString(stringRes)))))
        return this as T
    }

    fun checkViewDisplayedWithId(@IdRes viewId: Int): T {
        onView(withId(viewId)).check(matches(isDisplayed()))
        return this as T
    }

    fun checkViewWithIdDisplayedWithText(@IdRes viewId: Int, text: String): T {
        onView(withId(viewId)).check(matches(allOf(isDisplayed(), withText(text))))
        return this as T
    }

    fun checkViewWithTextIsDisplayed(@StringRes stringRes: Int): T {
        onView(
            withText(
                context!!.getString(
                    stringRes
                )
            )
        ).check(matches(isDisplayed()))
        return this as T
    }

    fun checkViewDisplayedWithTextId(@IdRes viewId: Int, @StringRes stringRes: Int): T {
        onView(withId(viewId)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(
                        context!!.getString(
                            stringRes
                        )
                    )
                )
            )
        )
        return this as T
    }

    fun provideContext(context: Context?): T {
        this.context = context
        return this as T
    }

    fun clickOnViewWithId(@IdRes viewId: Int): T {
        onView(withId(viewId)).perform(click())
        addExecutionDelay(300)
        return this as T
    }

    fun addExecutionDelay(millis: Long) {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    companion object {

        fun <T : ScreenRobot<*>> withRobot(screenRobotClass: Class<T>?): T {
            if (screenRobotClass == null) {
                throw IllegalArgumentException("instance class == null")
            }

            try {
                return screenRobotClass.newInstance()
            } catch (iae: IllegalAccessException) {
                throw RuntimeException("IllegalAccessException", iae)
            } catch (ie: InstantiationException) {
                throw RuntimeException("InstantiationException", ie)
            }
        }
    }
}
