package com.example.pillnotifier

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.pillnotifier.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager2: ViewPager2 = findViewById(R.id.view_pager_2)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)

        viewPager2.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager2) {
            tab, position -> tab.text = when(position) {
                0 -> "Dependents"
                1 -> "Schedule"
                2 -> "Medicine"
                3 -> "Explore"
                else -> "None"
            }
        }.attach()
    }
}