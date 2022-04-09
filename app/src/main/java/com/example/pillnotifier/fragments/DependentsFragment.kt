package com.example.pillnotifier.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.pillnotifier.Constants
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.DependentWithTakesAdapter
import com.example.pillnotifier.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.lang.IllegalArgumentException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RequiresApi(Build.VERSION_CODES.O)
class DependentsFragment : Fragment() {
    private val dependents: MutableList<DependentWithTakes> = mutableListOf()
    private lateinit var tvDate: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dependents, container, false)
        tvDate = view.findViewById(R.id.dependent_tvDate)
        val loading = view.findViewById<ProgressBar>(R.id.loading)
        val recyclerView: RecyclerView = view.findViewById(R.id.dependents_rv)
        val swipeRefresh: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)

        recyclerView.adapter = DependentWithTakesAdapter(requireContext(), dependents)
        recyclerView.addItemDecoration(DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        ))

        tvDate.setOnClickListener {
            val newFragment = DatePickerFragment(tvDate)
            newFragment.show(childFragmentManager, "Date Picker")
        }
        tvDate.doAfterTextChanged {
            val date = it.toString()
            dependents.clear()
            loading.visibility = View.VISIBLE
            lifecycleScope.launch {
                val result: DependentsResult = withContext(Dispatchers.IO) {
                    getInfoForUpdate(date)
                }
                if (result.success != null) {
                    dependents.addAll(result.success)
                    recyclerView.adapter!!.notifyDataSetChanged()
                } else if (result.error != null) {
                    Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                }
                loading.visibility = View.GONE
            }
        }

        swipeRefresh.setOnRefreshListener {
            if (tvDate.text.isNotEmpty()) {
                loading.visibility = View.VISIBLE
                dependents.clear()
                lifecycleScope.launch {
                    val result: DependentsResult = withContext(Dispatchers.IO) {
                        getInfoForUpdate(tvDate.toString())
                    }
                    if (result.success != null) {
                        dependents.addAll(result.success)
                        recyclerView.adapter!!.notifyDataSetChanged()
                    } else if (result.error != null) {
                        Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                    }
                }
                loading.visibility = View.GONE
            }
            swipeRefresh.isRefreshing = false
        }

        return view
    }

    private suspend fun getInfoForUpdate(date: String) : DependentsResult = suspendCoroutine { cont ->
        val client = OkHttpClient.Builder().build()

        val httpUrl: HttpUrl? = (Constants.BASE_URL + "/dependents").toHttpUrlOrNull()
        if (httpUrl == null) {
            cont.resume(DependentsResult(error = requireContext().resources.getString(R.string.dependents_failed)))
        }

        val httpUrlBuilder: HttpUrl.Builder = httpUrl!!.newBuilder()
        httpUrlBuilder.addQueryParameter("user_id", DataHolder.getData("userId"))
        httpUrlBuilder.addQueryParameter("date", date)


        lateinit var request: Request
        try {
            request = Request.Builder()
                .url(httpUrlBuilder.build())
                .build()
        } catch (e: IllegalArgumentException) {
            cont.resume(DependentsResult(error = e.message))
            return@suspendCoroutine
        }


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resume(DependentsResult(error = context!!.resources.getString(R.string.dependents_failed)))
            }

            override fun onResponse(call: Call, response: Response) {
                val message: String = response.body!!.string()
                val gson = Gson()
                if (response.code == 200) {
                    val medicineTakeList = gson.fromJson(message, Array<DependentWithTakes>::class.java).toMutableList()
                    cont.resume(DependentsResult(success = medicineTakeList))
                } else {
                    onFailure(call, IOException(message))
                }
            }
        })
    }
}