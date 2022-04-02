package com.example.pillnotifier.fragments

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.pillnotifier.Constants
import com.example.pillnotifier.MedicineProfile
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.MedicineAdapter
import com.example.pillnotifier.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RequiresApi(Build.VERSION_CODES.O)
class MedicineFragment : Fragment() {
    private val medicinesList: MutableList<Medicine> = mutableListOf()

    private fun showMedicinesRequestFailed(errorString: String) {
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
    }

    private class MedicineListResult(
        val success: MutableList<Medicine>? = null,
        val error: String? = null
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var medicineListLL: LinearLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var addButton: Button

    private suspend fun updateMedicineList() {
        addButton.visibility = View.GONE
        medicineListLL.visibility = View.GONE
        loadingProgressBar.visibility = View.VISIBLE
        val result: MedicineListResult = withContext(Dispatchers.IO) {
            suspendCoroutine { cont ->
                val client = OkHttpClient.Builder().build()

                val httpUrl: HttpUrl? = (Constants.BASE_URL + "/medicines").toHttpUrlOrNull()
                if (httpUrl == null) {
                    cont.resume(MedicineListResult(error = requireContext().resources.getString(R.string.medicines_failed)))
                }

                val httpUrlBuilder: HttpUrl.Builder = httpUrl!!.newBuilder()
                httpUrlBuilder.addQueryParameter("user_id", DataHolder.getData("userId"))

                lateinit var request: Request
                try {
                    request = Request.Builder()
                        .url(httpUrlBuilder.build())
                        .build()
                } catch (e: IllegalArgumentException) {
                    cont.resume(MedicineListResult(error = e.message))
                    return@suspendCoroutine
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        cont.resume(MedicineListResult(error = context!!.resources.getString(R.string.medicines_failed)))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val message: String = response.body!!.string()
                        val gson = Gson()
                        if (response.code == 200) {
                            val medList =
                                gson.fromJson(message, Array<Medicine>::class.java)
                                    .toMutableList()
                            cont.resume(MedicineListResult(success=medList))
                        } else {
                            onFailure(call, IOException(message))
                        }
                    }
                })
            }
        }
        if (result.success != null) {
            medicinesList.clear()
            medicinesList.addAll(result.success)
            recyclerView.adapter!!.notifyDataSetChanged()
        } else if (result.error != null) {
            showMedicinesRequestFailed(result.error)
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

        return view
    }
}