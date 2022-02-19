package com.example.pillnotifier

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MedicineProfile : AppCompatActivity() {
    var medicineName: String? = null
    var instruction: String? = null

    var medicineInput: EditText? = null
    var instructionInput: EditText? = null
    var submitButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_profile)

        medicineInput = findViewById<View>(R.id.input_medicine_name) as EditText
        instructionInput = findViewById<View>(R.id.input_medicine_instructions) as EditText
        submitButton = findViewById<View>(R.id.submitButton2) as Button
        val mySpinner = findViewById<View>(R.id.spinner1) as Spinner

        val myAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, resources.getStringArray(R.array.names)
        )
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        mySpinner.adapter = myAdapter
        val picker: TimePicker = findViewById(R.id.timePicker1)
        picker.setIs24HourView(true)
        submitButton!!.setOnClickListener {
            onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}