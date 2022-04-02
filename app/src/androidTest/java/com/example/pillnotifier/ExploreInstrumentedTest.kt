package com.example.pillnotifier

import android.view.View
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.pillnotifier.fragments.ExploreFragment
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.Profile
import com.example.pillnotifier.model.ProfilesList
import kotlinx.android.synthetic.main.incoming_request_item.view.*
import kotlinx.android.synthetic.main.outgoing_request_item.view.*
import kotlinx.android.synthetic.main.profiles_list.view.*
import kotlinx.android.synthetic.main.removable_user_list_item.view.*
import kotlinx.android.synthetic.main.user_item.view.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.RuntimeException


@RunWith(AndroidJUnit4::class)
class ExploreInstrumentedTest {
    @Before
    fun setUp() {
        DataHolder.setData("userId", "test_user_id")
    }

    @Rule
    @JvmField
    var mExploreFragment = ActivityTestRule(MainActivity::class.java)

    private fun checkDependents(
        posInLL: Int,
        profile: ProfilesList,
    ): Matcher<View?> {
        return object : BoundedMatcher<View?, View>(View::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("Checking profile item properties")
            }

            override fun matchesSafely(foundView: View): Boolean {
                if (foundView.profiles_lists_rv.size != profile.profiles.size) {
                    throw RuntimeException("I DON'T UNDERSTAND WHY ${foundView.profiles_lists_rv.size}" +
                            " ${profile.profiles.size} ${profile.list_name}")
                    return false
                }
                for ((pos, prof) in profile.profiles.withIndex()) {
                    if (!(foundView.profiles_lists_rv[pos].user_name_tv.text.equals(prof.name) &&
                                foundView.profiles_lists_rv[pos].user_nickname_tv.text.equals(prof.nickname) &&
                                when (posInLL) {
                                    0, 1 -> foundView.profiles_lists_rv[pos].remove_button.isVisible
                                    2 -> foundView.profiles_lists_rv[pos].decline_button.isVisible &&
                                            foundView.profiles_lists_rv[pos].accept_button.isVisible
                                    else -> foundView.profiles_lists_rv[pos].withdraw_button.isVisible
                                }
                                )) {
                            return false
                        }
                }
                return true
            }
        }

    }

    private fun checkExploreList(profiles: List<ProfilesList>) {
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        onView(withId(R.id.explore_rv)).check(matches(length(profiles.size)))
        for ((pos, profilesLL) in profiles.withIndex()) {
            onView(withId(R.id.explore_rv))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        pos,
                        clickChildViewWithId(R.id.show_meds_iv)
                    )
                )
                .check(
                    matches(
                        atPosition(
                            pos,
                            checkDependents(pos, profilesLL)
                        )
                    )
                )
        }
    }

    private fun withCustomConstraints(action: ViewAction, constraints: Matcher<View>): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return constraints
            }

            override fun getDescription(): String {
                return action.description
            }

            override fun perform(uiController: UiController?, view: View?) {
                action.perform(uiController, view)
            }
        }
    }

    private fun closeRecyclerView() {
        for (i in 0..3) {
            onView(withId(R.id.explore_rv))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        i,
                        clickChildViewWithId(R.id.show_meds_iv)
                    )
                )
        }
    }

    @Test
    fun testDefaultListView() {
        launchFragmentInContainer<ExploreFragment>()
        checkExploreList(
            listOf(
                ProfilesList(
                    "Dependents",
                    listOf(
                        Profile("snd_user", "snd_user"),
                        Profile("test_observer", "test_observer")
                    )
                ),
                ProfilesList("Observers", listOf()),
                ProfilesList(
                    "Incoming requests",
                    listOf(Profile("Sherlock Holmes", "sherlock_holmes"))
                ),
                ProfilesList("Outgoing requests", listOf())
            )
        )
    }

    @Test
    fun testAddAndDeleteNewDependent() {
        val profiles = mutableListOf(
            ProfilesList(
                "Dependents",
                listOf(
                    Profile("snd_user", "snd_user"),
                    Profile("test_observer", "test_observer")
                )
            ),
            ProfilesList("Observers", listOf()),
            ProfilesList(
                "Incoming requests",
                listOf(Profile("Sherlock Holmes", "sherlock_holmes"))
            ),
            ProfilesList("Outgoing requests", listOf())
        )
        launchFragmentInContainer<ExploreFragment>()
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        checkExploreList(
            profiles
        )

        onView(withId(R.id.dependent_input)).perform(
            ViewActions.clearText(),
            ViewActions.typeText("test_explore"),
            ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.search_iv)).perform(ViewActions.click())
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))

        closeRecyclerView()

        profiles[3] = ProfilesList(
            "Outgoing requests",
            listOf(Profile("test_explore", "test_explore"))
        )
        checkExploreList(
           profiles
        )

        onView(withId(R.id.explore_rv)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                3,
                clickChildViewOfChildRecycleViewItem(R.id.profiles_lists_rv, 0, R.id.withdraw_button)
            )
        )

        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))

        closeRecyclerView()
        profiles[3] = ProfilesList(
            "Outgoing requests",
            listOf()
        )
        checkExploreList(
            profiles
        )
    }

}