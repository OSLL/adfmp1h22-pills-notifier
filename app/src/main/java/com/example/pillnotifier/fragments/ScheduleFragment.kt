package com.example.pillnotifier.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.MedicineAdapter
import com.example.pillnotifier.model.Medicine
import com.example.pillnotifier.model.MedicineTake
import com.example.pillnotifier.model.Regularity
import java.util.*


class ScheduleFragment : Fragment() {
    private val medicineTakeList: MutableList<MedicineTake> = mutableListOf()

    init {
        // TODO: delete plug and make initialization of list
        val medicineTake = MedicineTake(
            Medicine("Vitamin A", "4 pills", Regularity.DAILY, Date(2022, 1, 1)),
            Date(2022, 1, 7)
        )
        for (i in 1..20)
            medicineTakeList.add(medicineTake)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_schedule_meds)
        recyclerView.adapter = MedicineAdapter(requireContext(), medicineTakeList)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        return view
    }

}