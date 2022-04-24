package com.example.pillnotifier.fragments

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.pillnotifier.MedicineProfile
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.MedicineAdapter
import com.example.pillnotifier.cachingMedicineList
import com.example.pillnotifier.getCachedMedicineList
import com.example.pillnotifier.model.*
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class MedicineFragment : Fragment() {
    private val medicinesList: MutableList<Medicine> = mutableListOf()

    private fun showMedicinesRequestFailed(errorString: String) {
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var medicineListLL: LinearLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var addButton: Button

    private suspend fun updateMedicineList() {
        addButton.visibility = View.GONE
        medicineListLL.visibility = View.GONE
        loadingProgressBar.visibility = View.VISIBLE
        val result: MedicineListResult = getMedicineListFromServer(requireContext())
        if (result.success != null) {
            medicinesList.clear()
            medicinesList.addAll(result.success)
            cachingMedicineList(requireContext(), medicinesList)
            recyclerView.adapter!!.notifyDataSetChanged()
        } else if (result.error != null) {
            showMedicinesRequestFailed(result.error)
            medicinesList.clear()
            medicinesList.addAll(getCachedMedicineList(requireContext()))
            recyclerView.adapter!!.notifyDataSetChanged()
        }
        loadingProgressBar.visibility = View.GONE
        addButton.visibility = View.VISIBLE
        medicineListLL.visibility = View.VISIBLE
    }

    private val activityForResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                updateMedicineList()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine, container, false)
        medicineListLL = view.findViewById(R.id.medicine_list)

        recyclerView = view.findViewById(R.id.rv_medicine)
        recyclerView.adapter = MedicineAdapter(requireContext(), medicinesList, activityForResultLauncher)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        loadingProgressBar = view.findViewById(R.id.loading)

        addButton = view.findViewById(R.id.add_button)
        addButton.setOnClickListener{
            val intent = Intent(it.context, MedicineProfile::class.java)
            intent.putExtra("mode", MedicineProfile.Mode.CREATE)
            activityForResultLauncher.launch(intent)
        }

        val swipeRefresh: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            lifecycleScope.launch {
                updateMedicineList()
                swipeRefresh.isRefreshing = false
            }
        }

        lifecycleScope.launch {
            updateMedicineList()
        }

        Log.d("MINE", "Medicine fragment initialized")
        return view
    }
}