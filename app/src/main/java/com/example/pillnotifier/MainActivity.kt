package com.example.pillnotifier

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.pillnotifier.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mToggle: ActionBarDrawerToggle
    private lateinit var mToolBar: Toolbar

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

        mDrawerLayout = findViewById(R.id.drawerLayout)
        mToggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close)
        mDrawerLayout.addDrawerListener(mToggle)
        mToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @Override
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}