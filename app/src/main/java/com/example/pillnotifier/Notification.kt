package com.example.pillnotifier

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pillnotifier.fragments.NotificationFragment

class Notification : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        if (savedInstanceState != null) {
            return
        }

        supportFragmentManager.beginTransaction().add(R.id.fragment_container,
            NotificationFragment(), null).commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}