package com.example.pillnotifier.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.Constants
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.NotificationAdapter
import com.example.pillnotifier.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class NotificationFragment : Fragment() {
    private val notificationList: MutableList<Notification> = mutableListOf()

    private fun showNotificationsRequestFailed(@StringRes errorString: Int) {
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_notification)
        recyclerView.adapter = NotificationAdapter(requireContext(), notificationList)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        val loading = view.findViewById<ProgressBar>(R.id.loading)
        loading.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result: NotificationResult = withContext(Dispatchers.IO) {
                suspendCoroutine { cont ->
                    val client = OkHttpClient.Builder().build()
                    Log.d("MY_LOG", "Sending request")

                    val httpUrl: HttpUrl? = (Constants.BASE_URL + "/notifications").toHttpUrlOrNull()
                    if (httpUrl == null) {
                        cont.resume(NotificationResult(error = R.string.notifications_failed))
                    }

                    val httpUrlBuilder: HttpUrl.Builder = httpUrl!!.newBuilder()
                    httpUrlBuilder.addQueryParameter("user_id", DataHolder.getData("userId"))

                    val request: Request = Request.Builder()
                        .url(httpUrlBuilder.build())
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("MY_LOG", "Failed")
                            cont.resume(NotificationResult(error = R.string.notifications_failed))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            Log.d("MY_LOG", "OK")
                            val message: String = response.body!!.string()
                            val gson = Gson()
                            if (response.code == 200) {
                                Log.d("MY_LOG", "OK1")
                                val notificationList = gson.fromJson(message, Array<Notification>::class.java).toMutableList()
                                cont.resume(NotificationResult(success = notificationList))
                            } else {
                                Log.d("MY_LOG", "Not OK")
                                onFailure(call, IOException(message))
                            }
                        }
                    })
                }
            }
            if (result.success != null) {
                notificationList.clear()
                notificationList.addAll(result.success)
                notificationList.reverse()
                recyclerView.adapter!!.notifyDataSetChanged()
            } else if (result.error != null) {
                showNotificationsRequestFailed(result.error)
            }
            loading.visibility = View.GONE
        }
        return view
    }
}