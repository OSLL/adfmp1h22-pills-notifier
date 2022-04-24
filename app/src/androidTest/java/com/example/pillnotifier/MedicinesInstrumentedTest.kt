package com.example.pillnotifier

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.pillnotifier.fragments.MedicineFragment
import com.example.pillnotifier.model.DataHolder
import kotlinx.android.synthetic.main.medicine_in_medicine_item.view.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class MedicinesInstrumentedTest {
    @Before
    fun setUp() {
        DataHolder.setData("userId", "test_medicine")
    }

    @Rule
    @JvmField
    var mScheduleFragment = ActivityTestRule(MainActivity::class.java)

    private fun checkMedicineItem(
        medicineNameTV: String,
        portionTV: String,
        regularityTV: String
    ): Matcher<View?> {
        return object : BoundedMatcher<View?, View>(View::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("Checking medicine item properties")
            }

            override fun matchesSafely(foundView: View): Boolean {
                return foundView.medicine_name.text.equals(medicineNameTV)
                        && foundView.medicine_portion.text.equals(portionTV)
                        && foundView.regularity_and_take_time.text.equals(regularityTV)
                        && foundView.button_delete.isVisible
                        && foundView.button_delete.isVisible
            }
        }
    }

    data class MedicineInfo(val name: String, val portion: String, val regularityWithTime: String)
    private fun checkMedicinesList(medicines: List<MedicineInfo>) {
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        onView(withId(R.id.rv_medicine)).check(matches(length(medicines.size)))
        for ((pos, medInfo) in medicines.withIndex()) {
            onView(withId(R.id.rv_medicine))
                .check(matches(atPosition(
                    pos,
                    checkMedicineItem(medInfo.name, medInfo.portion, medInfo.regularityWithTime)
                )))
        }
    }

    private fun setProfile(medicineName: String, portion: String, regularity: String,
                           startDate: LocalDate, endDate: LocalDate, takeTime: LocalTime) {
        onView(withId(R.id.input_medicine_name)).perform(
            clearText(),
            typeText(medicineName),
            closeSoftKeyboard()
        )
        onView(withId(R.id.input_medicine_portion)).perform(
            clearText(),
            typeText(portion),
            closeSoftKeyboard()
        )
        setSpinnerTextItem(R.id.regularity_spinner, regularity)
        setDate(R.id.tvStartDate, startDate.year, startDate.monthValue, startDate.dayOfMonth)
        setDate(R.id.tvEndDate, endDate.year, endDate.monthValue, endDate.dayOfMonth)
        setTime(R.id.tvTakeTime, takeTime.hour, takeTime.minute)
        onView(withId(R.id.submitMedicineButton)).perform(click())
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
    }
    @Test
    fun testDefaultListView() {
        launchFragmentInContainer<MedicineFragment>()
        checkMedicinesList(listOf(
            MedicineInfo("Vitamin A", "1 pill", "Daily at 16:00"),
            MedicineInfo("Vitamin B", "2 pills", "Daily at 13:00")
        ))
    }

    @Test
    fun testAddNewMedicineAndDeleteIt() {
        launchFragmentInContainer<MedicineFragment>()
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        checkMedicinesList(listOf(
            MedicineInfo("Vitamin A", "1 pill", "Daily at 16:00"),
            MedicineInfo("Vitamin B", "2 pills", "Daily at 13:00")
        ))
        onView(withId(R.id.add_button)).perform(click())

        setProfile("Vitamin C", "3 pills", "DAILY",
                   LocalDate.of(2022, 1, 1), LocalDate.of(2022, 4, 1),
                   LocalTime.of(8, 0))

        checkMedicinesList(listOf(
            MedicineInfo("Vitamin A", "1 pill", "Daily at 16:00"),
            MedicineInfo("Vitamin B", "2 pills", "Daily at 13:00"),
            MedicineInfo("Vitamin C", "3 pills", "Daily at 08:00")
        ))
        onView(withId(R.id.rv_medicine)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                2,
                clickChildViewWithId(R.id.button_delete)
            )
        )
        onView(withText("Are you sure?")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())
        checkMedicinesList(listOf(
            MedicineInfo("Vitamin A", "1 pill", "Daily at 16:00"),
            MedicineInfo("Vitamin B", "2 pills", "Daily at 13:00")
        ))
    }

    @Test
    fun testEditingMedicine() {
        launchFragmentInContainer<MedicineFragment>()
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        checkMedicinesList(listOf(
            MedicineInfo("Vitamin A", "1 pill", "Daily at 16:00"),
            MedicineInfo("Vitamin B", "2 pills", "Daily at 13:00")
        ))

        onView(withId(R.id.rv_medicine)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                clickChildViewWithId(R.id.button_edit)
            )
        )
        setProfile("Vitamin C", "3 pills", "DAILY",
            LocalDate.of(2022, 1, 1), LocalDate.of(2022, 4, 1),
            LocalTime.of(8, 0))
        checkMedicinesList(listOf(
            MedicineInfo("Vitamin C", "3 pills", "Daily at 08:00"),
            MedicineInfo("Vitamin B", "2 pills", "Daily at 13:00")
        ))

        onView(withId(R.id.rv_medicine)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                clickChildViewWithId(R.id.button_edit)
            )
        )
        setProfile("Vitamin A", "1 pill", "DAILY",
            LocalDate.of(2022, 1, 1), LocalDate.of(2022, 4, 1),
            LocalTime.of(16, 0))
        checkMedicinesList(listOf(
            MedicineInfo("Vitamin A", "1 pill", "Daily at 16:00"),
            MedicineInfo("Vitamin B", "2 pills", "Daily at 13:00")
        ))
    }

    @Test
    fun testReadingMedicine() {
        launchFragmentInContainer<MedicineFragment>()
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        checkMedicinesList(listOf(
            MedicineInfo("Vitamin A", "1 pill", "Daily at 16:00"),
            MedicineInfo("Vitamin B", "2 pills", "Daily at 13:00")
        ))
        onView(withId(R.id.rv_medicine))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(withId(R.id.input_medicine_name)).check(matches(withText("Vitamin A")))
        onView(withId(R.id.input_medicine_portion)).check(matches(withText("1 pill")))
        onView(withId(R.id.regularity_spinner)).check(matches(withSpinnerText("DAILY")))
        onView(withId(R.id.tvStartDate)).check(matches(withText("2022-01-01")))
        onView(withId(R.id.tvEndDate)).check(matches(withText("2022-06-01")))
        onView(withId(R.id.tvTakeTime)).check(matches(withText("16:00")))
        onView(withId(R.id.submitMedicineButton)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        pressBack()
    }
}