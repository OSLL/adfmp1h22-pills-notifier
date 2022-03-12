package com.example.pillnotifier

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.pillnotifier.adapters.ViewPagerAdapter
import com.example.pillnotifier.model.DataHolder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mToggle: ActionBarDrawerToggle

    private val activityWithResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data?.hasExtra("username") == true) {
                    val username = findViewById<View>(R.id.username) as TextView
                    username.text = result.data!!.extras?.getString("username")
                    DataHolder.setData("username", result.data!!.extras?.getString("username"))
                }
                if (result.data?.hasExtra("link") == true) {
                    val link = findViewById<View>(R.id.link) as TextView
                    link.text = result.data!!.extras?.getString("link")
                    DataHolder.setData("link", result.data!!.extras?.getString("link"))
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager2: ViewPager2 = findViewById(R.id.view_pager_2)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager2.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager2) {
            tab, position ->
            val iconId = when (position) {
                0 -> R.drawable.dependents_icon
                1 -> R.drawable.schedulte_icon
                2 -> R.drawable.medicine_icon
                3 -> R.drawable.explore_icon
                else -> -1
            }
            tab.setIcon(iconId)
        }.attach()

        mDrawerLayout = findViewById(R.id.drawerLayout)
        mToggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close)
        mDrawerLayout.addDrawerListener(mToggle)
        mToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mNavigationView = findViewById<View>(R.id.nav_menu) as NavigationView
        val headerView: View = mNavigationView.getHeaderView(0)
        val username = headerView.findViewById<View>(R.id.username) as TextView
        username.text = DataHolder.getData("fullname")

        val link = headerView.findViewById<View>(R.id.link) as TextView
        link.text = DataHolder.getData("username")

        mNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_profile -> {
                    activityWithResult.launch(Intent(applicationContext, Settings::class.java))
                }
            }
            true
        }
    }

    @Override
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mToggle.onOptionsItemSelected(item)) {
            return true
        }
        val id = item.itemId
        if (id == R.id.notification) {
            activityWithResult.launch(Intent(applicationContext, Notification::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}