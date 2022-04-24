package com.example.pillnotifier

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.pillnotifier.adapters.ViewPagerAdapter
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.RequestResult
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mToggle: ActionBarDrawerToggle
    private var new_notification = AtomicBoolean(false)

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

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
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
        username.text = DataHolder.getData("username")

        val link = headerView.findViewById<View>(R.id.link) as TextView
        link.text = DataHolder.getData("link")

        mNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_profile -> {
                    activityWithResult.launch(Intent(applicationContext, Settings::class.java))
                }
            }
            true
        }

        thread(start = true, isDaemon = true) {
            while (true) {
                sleep(5000)
                lifecycleScope.launch {
                    val result: RequestResult = withContext(Dispatchers.IO) {
                        suspendCoroutine { cont ->
                            val client = OkHttpClient.Builder().build()

                            val httpUrl: HttpUrl? =
                                (Constants.BASE_URL + "/notification_status").toHttpUrlOrNull()
                            if (httpUrl == null) {
                                cont.resume(RequestResult(error = "Error while fetching notifications: invalid server http url"))
                            }

                            val httpUrlBuilder: HttpUrl.Builder = httpUrl!!.newBuilder()
                            httpUrlBuilder.addQueryParameter(
                                "user_id",
                                DataHolder.getData("userId")
                            )

                            val request: Request = Request.Builder()
                                .url(httpUrlBuilder.build())
                                .build()

                            client.newCall(request).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    cont.resume(RequestResult(error = "Failed to update notifications status"))
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val message: String = response.body!!.string()
                                    if (response.code == 200) {
                                        cont.resume(RequestResult(success = message))
                                    } else {
                                        onFailure(call, IOException(message))
                                    }
                                }
                            })
                        }
                    }
                    if (result.success != null) {
                        new_notification.set(Objects.equals("true", result.success))
                    }
                }
                this.invalidateOptionsMenu()
            }
        }

        cachingCurrentUserId(this)
        WorkManager.getInstance(this).cancelAllWorkByTag(NotificationWorker.NOTIFY_WORK_TAG)
        val myWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInitialDelay(20, TimeUnit.SECONDS)
            .addTag(NotificationWorker.NOTIFY_WORK_TAG).build()
        WorkManager.getInstance(this).enqueue(myWorkRequest)
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val resId: Int
        if (new_notification.get()) {
            resId = resources.getIdentifier(
                "ic_baseline_notification_important_24", "drawable",
                packageName
            )
        } else {
            resId = resources.getIdentifier(
                "baseline_notifications_24", "drawable",
                packageName
            )
        }
        if (resId != 0) menu.findItem(R.id.notification).setIcon(resId)
        return true
    }
}