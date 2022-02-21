package com.example.pillnotifier.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.MedicineTakeAdapter
import com.example.pillnotifier.adapters.Rights
import com.example.pillnotifier.model.Medicine
import com.example.pillnotifier.model.MedicineTake
import com.example.pillnotifier.model.Regularity
import com.example.pillnotifier.model.TakeStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class ScheduleFragment : Fragment() {
    private val medicineTakeList: MutableList<MedicineTake> = mutableListOf()

    init {
        // TODO: delete plug and make initialization of list
        val medicine = Medicine("Vitamin A", "4 pills", LocalTime.of(8, 0), Regularity.DAILY,
            LocalDate.of(2022, Month.JANUARY, 1))
        val date = LocalDate.of(2022, Month.JANUARY, 7)
        medicineTakeList.add(MedicineTake(medicine, TakeStatus.TAKEN))
        medicineTakeList.add(MedicineTake(medicine, TakeStatus.NOT_TAKEN))
        medicineTakeList.add(MedicineTake(medicine, TakeStatus.UNKNOWN))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        tvDate.setOnClickListener {
            val newFragment = DatePickerFragment(tvDate)
            newFragment.show(childFragmentManager, "Date Picker")
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_schedule_meds)
        recyclerView.adapter = MedicineTakeAdapter(requireContext(), medicineTakeList, Rights.WRITE)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        return view
    }

}