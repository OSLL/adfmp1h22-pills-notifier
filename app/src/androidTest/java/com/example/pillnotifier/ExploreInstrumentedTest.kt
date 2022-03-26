package com.example.pillnotifier

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.pillnotifier.fragments.ExploreFragment
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.Profile
import com.example.pillnotifier.model.ProfilesList
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
                description.appendText("Checking medicine item properties")
            }

            override fun matchesSafely(foundView: View): Boolean {
                return profile.profiles.all { foundView.user_name_tv.text.equals(it.name) }
//                        && foundView.user_nickname_tv.text.equals(profile.nickname)
//                        && foundView.remove_button.isVisible
            }
        }

    }


    data class ProfileInfo(val name: String, val nickname: String)

    private fun checkMedicinesList(profiles: List<ProfilesList>) {
        onView(isRoot()).perform(waitUntilNotShown(R.id.loading, 10000))
        onView(withId(R.id.explore_rv)).check(matches(length(profiles.size)))
        for ((pos, profilesLL) in profiles.withIndex()) {
            onView(withId(R.id.explore_rv))
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
        checkMedicinesList(
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