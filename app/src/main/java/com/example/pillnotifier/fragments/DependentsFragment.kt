package com.example.pillnotifier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.DependentWithTakesAdapter
import com.example.pillnotifier.model.*
import java.util.*

class DependentsFragment : Fragment() {
    private val dependents: MutableList<DependentWithTakes> = mutableListOf()

    init {
        val medicineTakeList: MutableList<MedicineTake> = mutableListOf()
        val medicine = Medicine("Vitamin A", "4 pills", Regularity.DAILY, Date(2022, 1, 1))
        val date = Date(2022, 1, 7)
        medicineTakeList.add(MedicineTake(medicine, date, TakeStatus.TAKEN))
        medicineTakeList.add(MedicineTake(medicine, date, TakeStatus.NOT_TAKEN))
        medicineTakeList.add(MedicineTake(medicine, date, TakeStatus.UNKNOWN))

        val dependentWithTakes = DependentWithTakes(
            "Kimberly White",
            "kwhite",
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