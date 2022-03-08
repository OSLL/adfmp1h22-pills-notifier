package com.example.pillnotifier.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DatePickerFragment(val tvDate: TextView) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var calendar : Calendar
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            android.R.style.ThemeOverlay_Material_Dialog,
            this,
            year,
            month,
            day
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDateSet(p0: DatePicker?, y: Int, m: Int, d: Int) {
        tvDate.text = LocalDate.of(y, m + 1, d)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString()
    }

}