package com.example.pillnotifier

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.pillnotifier.fragments.ScheduleFragment
import com.example.pillnotifier.model.DataHolder
import kotlinx.android.synthetic.main.medicine_in_schedule_item.view.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ScheduleInstrumentedTest {
    @Before
    fun setUp() {
        DataHolder.setData("userId", "test_schedule")
    }

    @Rule
    @JvmField
    var mScheduleFragment = ActivityTestRule(MainActivity::class.java)

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

    @Test
    fun testNonEmptySchedule() {
        launchFragmentInContainer<ScheduleFragment>()
        setDate(R.id.tvDate, 2022, 3, 1)
        onView(withId(R.id.tvDate)).check(matches(withText("2022-03-01")))
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        onView(withId(R.id.rv_schedule_meds)).check(matches(length(3)))
        onView(withId(R.id.rv_schedule_meds))
            .check(matches(
                    atPosition(0, checkScheduleItem(
                            "Vitamin A",
                            "1 pill",
                            "16:00",
                            View.VISIBLE,
                            View.VISIBLE
                    )))
            )
        onView(withId(R.id.rv_schedule_meds))
            .check(matches(
                atPosition(1, checkScheduleItem(
                    "Vitamin B",
                    "2 pills",
                    "13:00",
                    View.VISIBLE,
                    View.GONE
                )))
            )
        onView(withId(R.id.rv_schedule_meds))
            .check(matches(
                atPosition(2, checkScheduleItem(
                    "Vitamin C",
                    "3 pills",
                    "12:00",
                    View.GONE,
                    View.VISIBLE
                )))
            )
    }
    @Test
    fun testEmptySchedule() {
        launchFragmentInContainer<ScheduleFragment>()
        setDate(R.id.tvDate, 1999, 1, 1)
        onView(withId(R.id.tvDate)).check(matches(withText("1999-01-01")))
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        onView(withId(R.id.rv_schedule_meds)).check(matches(length(0)))
    }
}