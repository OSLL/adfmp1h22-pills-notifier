package com.example.pillnotifier.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import java.util.*


class ScheduleFragment : Fragment() {
    private val medicineTakeList: MutableList<MedicineTake> = mutableListOf()

    init {
        // TODO: delete plug and make initialization of list
        val medicine = Medicine("Vitamin A", "4 pills", Regularity.DAILY, Date(2022, 1, 1))
        val date = Date(2022, 1, 7)
        medicineTakeList.add(MedicineTake(medicine, date, TakeStatus.TAKEN))
        medicineTakeList.add(MedicineTake(medicine, date, TakeStatus.NOT_TAKEN))
        medicineTakeList.add(MedicineTake(medicine, date, TakeStatus.UNKNOWN))
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