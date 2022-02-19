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
import com.example.pillnotifier.model.MedicineInfo
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
class MedicineFragment : Fragment() {
    private val medicine: MutableList<MedicineInfo> = mutableListOf()

    init {
        val medicineInfo = MedicineInfo("Vitamin A", "Take 2 pills", LocalTime.of(13, 15))
        for (i in 1..20)
            medicine.add(medicineInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        recyclerView.adapter = MedicineAdapter(requireContext(), medicine)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        return view
    }
}