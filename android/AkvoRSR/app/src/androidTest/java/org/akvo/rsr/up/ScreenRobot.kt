package org.akvo.rsr.up

import android.app.Activity
import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

@Suppress("UNCHECKED_CAST")
abstract class ScreenRobot<T : ScreenRobot<T>> {

    private var context: Context? = null

    fun checkTitleIs(@StringRes stringRes: Int) : T {
        onView(allOf(isAssignableFrom(AppCompatTextView::class.java),
            withParent(isAssignableFrom(Toolbar::class.java))))
            .check(matches(withText((context!!.getString(stringRes)))))
        return this as T
    }

    fun checkViewDisplayedWithId(@IdRes viewId: Int): T {
        onView(withId(viewId)).check(matches(isDisplayed()))
        return this as T
    }

    fun checkViewDisplayedWithText(@IdRes viewId: Int, text: String): T {
        onView(withId(viewId)).check(matches(allOf(isDisplayed(), withText(text))))
        return this as T
    }

    fun checkViewDisplayedWithTextId(@IdRes viewId: Int, @StringRes stringRes: Int): T {
        onView(withId(viewId)).check(matches(allOf(isDisplayed(), withText(context!!.getString(stringRes)))))
        return this as T
    }

    fun provideContext(context: Context?): T {
        this.context = context
        return this as T
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
