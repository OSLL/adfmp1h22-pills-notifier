package com.example.pillnotifier.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.DependentWithTakesAdapter
import com.example.pillnotifier.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month

@RequiresApi(Build.VERSION_CODES.O)
class DependentsFragment : Fragment() {
    private val dependents: MutableList<DependentWithTakes> = mutableListOf()

    init {
        val medicineTakeList: MutableList<MedicineTake> = mutableListOf()
        val medicine = Medicine("","Vitamin A", "4 pills", LocalTime.of(8, 0), Regularity.DAILY,
            LocalDate.of(2022, Month.JANUARY, 1), LocalDate.of(2022, Month.JANUARY, 1))
        medicineTakeList.add(MedicineTake(medicine, TakeStatus.TAKEN, "2021-04-05"))
        medicineTakeList.add(MedicineTake(medicine, TakeStatus.NOT_TAKEN, "2021-04-05"))
        medicineTakeList.add(MedicineTake(medicine, TakeStatus.UNKNOWN, "2021-04-05"))

        val dependentWithTakes = DependentWithTakes(
            Profile("Kimberly White", "kwhite"),
            medicineTakeList
        )
        for (i in 1..2)
            dependents.add(dependentWithTakes)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dependents, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.dependents_rv)
        recyclerView.adapter = DependentWithTakesAdapter(requireContext(), dependents)
        recyclerView.addItemDecoration(DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        ))

        return view
    }

}