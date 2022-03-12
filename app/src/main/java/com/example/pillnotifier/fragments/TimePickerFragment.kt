package com.example.pillnotifier.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(val tvTime: TextView) : DialogFragment(), TimePickerDialog.OnTimeSetListener {
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

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        tvTime.text = "$p1:$p2"
    }
}