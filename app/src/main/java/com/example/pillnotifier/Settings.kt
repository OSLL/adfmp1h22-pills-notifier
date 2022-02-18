package com.example.pillnotifier

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class Settings : AppCompatActivity() {
    var name: String? = null
    var link: String? = null

    var nameInput: EditText? = null
    var linkInput: EditText? = null

    var submitButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        nameInput = findViewById<View>(R.id.name) as EditText
        linkInput = findViewById<View>(R.id.link_edit) as EditText
        submitButton = findViewById<View>(R.id.submitButton) as Button
        submitButton!!.setOnClickListener {
            name = nameInput?.text.toString()
            link = linkInput?.text.toString()
            val intent = Intent()
            if (name != null && name != "") {
                intent.putExtra("username", name)
            }
            if (link != null && link != "") {
                intent.putExtra("link", link)
            }
            setResult(Activity.RESULT_OK, intent)
            onBackPressed()
        }
    }
}