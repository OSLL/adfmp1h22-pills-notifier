package com.example.pillnotifier.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pillnotifier.fragments.DependentsFragment
import com.example.pillnotifier.fragments.ExploreFragment
import com.example.pillnotifier.fragments.MedicineFragment
import com.example.pillnotifier.fragments.ScheduleFragment


class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> DependentsFragment()
            1 -> ScheduleFragment()
            2 -> MedicineFragment()
            3 -> ExploreFragment()
            else -> Fragment()
        }
    }

    override fun getItemCount(): Int {
        return 4;
    }

}