package com.example.pillnotifier.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.Constants
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.MedicineTakeAdapter
import com.example.pillnotifier.adapters.Rights
import com.example.pillnotifier.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@RequiresApi(Build.VERSION_CODES.O)
class ScheduleFragment : Fragment() {
    private val medicineTakeList: MutableList<MedicineTake> = mutableListOf()

    private fun showScheduleRequestFailed(errorString: String) {
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val loading = view.findViewById<ProgressBar>(R.id.loading)
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_schedule_meds)
        tvDate.setOnClickListener {
            val newFragment = DatePickerFragment(tvDate)
            newFragment.show(childFragmentManager, "Date Picker")
        }
        tvDate.doAfterTextChanged {
            val date = it.toString()
            medicineTakeList.clear()
            loading.visibility = View.VISIBLE
            lifecycleScope.launch {
                val result: ScheduleResult = getScheduleFromServer(requireContext(), date)
                if (result.success != null) {
                    medicineTakeList.addAll(result.success)
                    recyclerView.adapter!!.notifyDataSetChanged()
                } else if (result.error != null) {
                    showScheduleRequestFailed(result.error)
                }
                loading.visibility = View.GONE
            }
        }

        recyclerView.adapter = MedicineTakeAdapter(requireContext(), medicineTakeList, Rights.WRITE)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        return view
    }

}