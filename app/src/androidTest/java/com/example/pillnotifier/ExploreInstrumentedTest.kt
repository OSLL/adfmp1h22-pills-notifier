package com.example.pillnotifier

import android.view.View
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
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
import kotlinx.android.synthetic.main.profiles_list.view.*
import kotlinx.android.synthetic.main.removable_user_list_item.view.*
import kotlinx.android.synthetic.main.user_item.view.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
        profile: ProfilesList,
    ): Matcher<View?> {
        return object : BoundedMatcher<View?, View>(View::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("Checking profile item properties")
            }

            override fun matchesSafely(foundView: View): Boolean {
                for ((pos, prof) in profile.profiles.withIndex()) {
                    if (!(foundView.profiles_lists_rv[pos].user_name_tv.text.equals(prof.name) &&
                                foundView.profiles_lists_rv[pos].user_nickname_tv.text.equals(prof.nickname) &&
                                foundView.profiles_lists_rv[pos].remove_button.isVisible)) {
                        return false
                    }
                }
                return true
            }
        }

    }


    data class ProfileInfo(val name: String, val nickname: String)

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
                            checkDependents(profilesLL)
                        )
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
                ProfilesList("Incoming requests", listOf()),
                ProfilesList("Outgoing requests", listOf())
            )
        )
    }
}