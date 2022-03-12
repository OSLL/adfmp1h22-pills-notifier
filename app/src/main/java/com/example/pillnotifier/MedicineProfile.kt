package com.example.pillnotifier

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pillnotifier.fragments.DatePickerFragment
import com.example.pillnotifier.fragments.TimePickerFragment


class MedicineProfile : AppCompatActivity() {
    private var medicineInput: EditText? = null
    private var portionInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_profile)

        medicineInput = findViewById<View>(R.id.input_medicine_name) as EditText
        portionInput = findViewById<View>(R.id.input_medicine_portion) as EditText

        val submitButton = findViewById<View>(R.id.submitButton2) as Button
        val mySpinner = findViewById<View>(R.id.spinner1) as Spinner

        val myAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, resources.getStringArray(R.array.names)
        )
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = myAdapter

        val startDate = findViewById<TextView>(R.id.tvStartDate)
        startDate.setOnClickListener {
            val newFragment = DatePickerFragment(startDate)
            newFragment.show(supportFragmentManager, "Start date picker")
        }

        val endDate = findViewById<TextView>(R.id.tvEndDate)
        endDate.setOnClickListener {
            val newFragment = DatePickerFragment(endDate)
            newFragment.show(supportFragmentManager, "End date picker")
        }

        val takeTime = findViewById<TextView>(R.id.tvTakeTime)
        takeTime.setOnClickListener {
            val newFragment = TimePickerFragment(takeTime)
            newFragment.show(supportFragmentManager, "Time picker")
        }
        submitButton!!.setOnClickListener {
            // TODO call on server
            onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}