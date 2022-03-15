package com.example.pillnotifier.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
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

    private fun showMedicinesRequestFailed(@StringRes errorString: Int) {
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
    }

    private class MedicineListResult(
        val success: MutableList<Medicine>? = null,
        val error: Int? = null
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine, container, false)
        val medicineListLL: LinearLayout = view.findViewById(R.id.medicine_list)
        medicineListLL.visibility = View.GONE

        val addButton: Button = view.findViewById(R.id.add_button)
        addButton.setOnClickListener{
            val intent = Intent(it.context, MedicineProfile::class.java)
            intent.putExtra("mode", MedicineProfile.Mode.CREATE)
            it.context.startActivity(intent)
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_medicine)
        recyclerView.adapter = MedicineAdapter(requireContext(), medicinesList)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        val loading = view.findViewById<ProgressBar>(R.id.loading)

        loading.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result: MedicineListResult = withContext(Dispatchers.IO) {
                suspendCoroutine { cont ->
                    val client = OkHttpClient.Builder().build()

                    val httpUrl: HttpUrl? = (Constants.BASE_URL + "/medicines").toHttpUrlOrNull()
                    if (httpUrl == null) {
                        cont.resume(MedicineListResult(error = R.string.schedule_failed))
                    }

                    val httpUrlBuilder: HttpUrl.Builder = httpUrl!!.newBuilder()
                    httpUrlBuilder.addQueryParameter("user_id", DataHolder.getData("userId"))

                    val request: Request = Request.Builder()
                        .url(httpUrlBuilder.build())
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            cont.resume(MedicineListResult(error = R.string.schedule_failed))
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
                medicinesList.addAll(result.success)
                recyclerView.adapter!!.notifyDataSetChanged()
            } else if (result.error != null) {
                showMedicinesRequestFailed(result.error)
            }
            loading.visibility = View.GONE
            medicineListLL.visibility = View.VISIBLE
        }

        return view
    }
}