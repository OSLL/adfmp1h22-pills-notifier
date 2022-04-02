package com.example.pillnotifier.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimePickerFragment(private val tvTime: TextView) : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    private lateinit var calendar : Calendar
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(
            requireContext(),
            android.R.style.ThemeOverlay_Material_Dialog,
            this,
            hour,
            minute,
            true
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        tvTime.text = LocalTime.of(p1, p2).format(DateTimeFormatter.ofPattern("HH:mm")).toString()
    }
}