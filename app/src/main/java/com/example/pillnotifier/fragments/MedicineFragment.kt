package com.example.pillnotifier.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.MedicineProfile
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.MedicineAdapter
import com.example.pillnotifier.model.Medicine
import com.example.pillnotifier.model.Regularity
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month

@RequiresApi(Build.VERSION_CODES.O)
class MedicineFragment : Fragment() {
    private val medicinesList: MutableList<Medicine> = mutableListOf()

    init {
        val medicine = Medicine("Vitamin A", "4 pills", LocalTime.of(8, 0), Regularity.DAILY,
            LocalDate.of(2022, Month.JANUARY, 1), LocalDate.of(2022, Month.JANUARY, 1))
        for (i in 1..20)
            medicinesList.add(medicine)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine, container, false)
        val addButton: Button = view.findViewById(R.id.add_button)
        addButton.setOnClickListener{
            val intent = Intent(it.context, MedicineProfile::class.java)
            it.context.startActivity(intent)
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_medicine)
        recyclerView.adapter = MedicineAdapter(requireContext(), medicinesList)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        return view
    }
}