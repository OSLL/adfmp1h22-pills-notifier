package com.example.pillnotifier

import android.view.View
import android.widget.DatePicker
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.pillnotifier.fragments.ScheduleFragment
import com.example.pillnotifier.model.DataHolder
import kotlinx.android.synthetic.main.medicine_in_schedule_item.view.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException


@RunWith(AndroidJUnit4::class)
class ScheduleInstrumentedTest {
    @Before
    fun setUp() {
        DataHolder.setData("userId", "test_schedule")
    }

    @Rule
    @JvmField
    var mScheduleFragment = ActivityTestRule(MainActivity::class.java)

    private fun waitUntilNotShown(viewId: Int, millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isRoot()
            }

            override fun getDescription(): String {
                return "Time limit exceeded"
            }

            override fun perform(uiController: UiController, view: View?) {
                uiController.loopMainThreadUntilIdle()
                val startTime = System.currentTimeMillis()
                val endTime = startTime + millis
                val viewMatcher: Matcher<View> = withId(viewId)
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

    private fun setDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(R.id.tvDate)).perform(
            click()
        )
        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
            PickerActions.setDate(
                year,
                monthOfYear,
                dayOfMonth
            )
        )
        onView(withId(android.R.id.button1)).perform(click())
    }

    private fun atPosition(position: Int, itemMatcher: Matcher<View?>): Matcher<View?> {
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

    private fun checkScheduleItem(
        medicineNameTV: String,
        portionTV: String,
        takeTimeTV: String,
        takenVisibility: Int,
        notTakenVisibility: Int
    ): Matcher<View?> {
        return object : BoundedMatcher<View?, View>(View::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("Checking schedule item properties")
            }

            override fun matchesSafely(foundView: View): Boolean {
                return foundView.medicine_name_tv.text.equals(medicineNameTV)
                        && foundView.portion_tv.text.equals(portionTV)
                        && foundView.take_time_tv.text.equals(takeTimeTV)
                        && foundView.taken_iv.visibility.equals(takenVisibility)
                        && foundView.not_taken_iv.visibility.equals(notTakenVisibility)
            }
        }
    }

    private fun length(length: Int): Matcher<View?> {
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("length is $length: ")
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                return view.adapter?.itemCount == length
            }
        }
    }

    @Test
    fun testNonEmptySchedule() {
        launchFragmentInContainer<ScheduleFragment>()
        setDate(2022, 3, 1)
        onView(withId(R.id.tvDate)).check(matches(withText("2022-03-01")))
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        onView(withId(R.id.rv_schedule_meds)).check(matches(length(3)))
        onView(withId(R.id.rv_schedule_meds))
            .check(matches(
                    atPosition(0, checkScheduleItem(
                            "Vitamin B",
                            "portion",
                            "16:00",
                            View.VISIBLE,
                            View.VISIBLE
                    )))
            )
        onView(withId(R.id.rv_schedule_meds))
            .check(matches(
                atPosition(1, checkScheduleItem(
                    "Vitamin A",
                    "portion",
                    "13:00",
                    View.VISIBLE,
                    View.GONE
                )))
            )
        onView(withId(R.id.rv_schedule_meds))
            .check(matches(
                atPosition(2, checkScheduleItem(
                    "Vitamin C",
                    "portion",
                    "12:00",
                    View.GONE,
                    View.VISIBLE
                )))
            )
    }
    @Test
    fun testEmptySchedule() {
        launchFragmentInContainer<ScheduleFragment>()
        setDate(1999, 1, 1)
        onView(withId(R.id.tvDate)).check(matches(withText("1999-03-01")))
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        onView(withId(R.id.rv_schedule_meds)).check(matches(length(0)))
    }
}