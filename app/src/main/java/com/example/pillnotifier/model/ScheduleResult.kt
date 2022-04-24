package com.example.pillnotifier.model

import android.content.Context
import com.example.pillnotifier.Constants
import com.example.pillnotifier.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.lang.IllegalArgumentException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class ScheduleResult(
    val success: MutableList<MedicineTake>? = null,
    val error: String? = null
)

suspend fun getScheduleFromServer(context: Context, date: String): ScheduleResult {
    return withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            val client = OkHttpClient.Builder().build()

            val httpUrl: HttpUrl? = (Constants.BASE_URL + "/schedule").toHttpUrlOrNull()
            if (httpUrl == null) {
                cont.resume(ScheduleResult(error = context.resources.getString(R.string.schedule_failed)))
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
                cont.resume(ScheduleResult(error = e.message))
                return@suspendCoroutine
            }

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resume(ScheduleResult(error = context.resources.getString( R.string.schedule_failed)))
                }

                override fun onResponse(call: Call, response: Response) {
                    val message: String = response.body!!.string()
                    val gson = Gson()
                    if (response.code == 200) {
                        val medicineTakeList = gson.fromJson(message, Array<MedicineTake>::class.java).toMutableList()
                        cont.resume(ScheduleResult(success = medicineTakeList))
                    } else {
                        onFailure(call, IOException(message))
                    }
                }
            })
        }
    }
}