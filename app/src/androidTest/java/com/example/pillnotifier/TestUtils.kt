package com.example.pillnotifier

import android.R.id
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.util.concurrent.TimeoutException


fun waitUntilNotShown(viewId: Int, millis: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isRoot()
        }

        override fun getDescription(): String {
            return "Time limit exceeded"
        }

        override fun perform(uiController: UiController, view: View?) {
            uiController.loopMainThreadUntilIdle()
            val startTime = System.currentTimeMillis()
            val endTime = startTime + millis
            val viewMatcher: Matcher<View> = ViewMatchers.withId(viewId)
            do {
                for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                    // found view with required ID
                    if (viewMatcher.matches(child) && !child.isShown) {
                        return
                    }
                }
                uiController.loopMainThreadForAtLeast(50)
            } while (System.currentTimeMillis() < endTime)
            throw PerformException.Builder()
                .withViewDescription(HumanReadables.describe(view))
                .withCause(TimeoutException())
                .build()
        }
    }
}

fun length(length: Int): Matcher<View?> {
    return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("length is $length: ")
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            return view.adapter?.itemCount == length
        }
    }
}

fun atPosition(position: Int, itemMatcher: Matcher<View?>): Matcher<View?> {
    return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has item at position $position: ")
            itemMatcher.describeTo(description)
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            val viewHolder = view.findViewHolderForAdapterPosition(position) ?: return false
            return itemMatcher.matches(viewHolder.itemView)
        }
    }
}

fun setDate(resourceId: Int, year: Int, monthOfYear: Int, dayOfMonth: Int) {
    onView(ViewMatchers.withId(resourceId)).perform(
        click()
    )
    onView(ViewMatchers.withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
        PickerActions.setDate(
            year,
            monthOfYear,
            dayOfMonth
        )
    )
    onView(ViewMatchers.withId(android.R.id.button1)).perform(click())
}

fun setTime(resourceId: Int, hour: Int, minute: Int) {
    onView(ViewMatchers.withId(resourceId)).perform(
        click()
    )
    onView(ViewMatchers.withClassName(Matchers.equalTo(TimePicker::class.java.name))).perform(
        PickerActions.setTime(
            hour,
            minute
        )
    )
    onView(ViewMatchers.withId(android.R.id.button1)).perform(click())
}

fun setSpinnerTextItem(resourceId: Int, textItem: String) {
    onView(ViewMatchers.withId(resourceId)).perform(
        click()
    )
    onView(ViewMatchers.withText(textItem)).perform(click())
}

fun clickChildViewWithId(resourceId: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View>? {
            return null
        }

        override fun getDescription(): String {
            return "Click on a child view with specified id."
        }

        override fun perform(uiController: UiController, view: View?) {
            val v: View = view!!.findViewById(resourceId)
            v.performClick()
        }
    }
}