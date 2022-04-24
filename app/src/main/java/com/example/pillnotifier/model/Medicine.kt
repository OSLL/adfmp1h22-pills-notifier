package com.example.pillnotifier.model

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.pillnotifier.Constants
import com.example.pillnotifier.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.DurationUnit

typealias Portion = String

@RequiresApi(Build.VERSION_CODES.O)
val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@RequiresApi(Build.VERSION_CODES.O)
val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

enum class Regularity(val stringInterpretation: String, val timeDelta: Period) {
    @RequiresApi(Build.VERSION_CODES.O)
    DAILY("Daily", Period.ofDays(1)),
    @RequiresApi(Build.VERSION_CODES.O)
    ONCE_IN_TWO_DAYS("Once in 2 days", Period.ofDays(2)),
    @RequiresApi(Build.VERSION_CODES.O)
    ONCE_A_WEEK("Once a week", Period.ofWeeks(1))
}

class Medicine : Serializable {
    var medicine_id: String? = null
    var medicine_name: String? = null
    var portion: Portion? = null
    var time: String? = null
    var regularity: Regularity? = null
    var start_date: String? = null
    var end_date: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(
        medicine_id: String,
        medicine_name: String,
        portion: Portion,
        time: String,
        regularity: Regularity,
        start_date: String,
        end_date: String
    ) {
        this.medicine_id = medicine_id
        this.medicine_name = medicine_name
        this.portion = portion
        this.regularity = regularity
        this.time = time
        this.start_date = start_date
        this.end_date = end_date
    }

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(
        medicine_id: String,
        medicine_name: String,
        portion: Portion,
        time: LocalTime,
        regularity: Regularity,
        start_date: LocalDate,
        end_date: LocalDate
    ) :
            this(
                medicine_id = medicine_id, medicine_name = medicine_name, portion = portion,
                time = time.format(timeFormat), regularity = regularity,
                start_date = start_date.format(dateFormat), end_date = end_date.format(dateFormat)
            )
}

class MedicineListResult(
    val success: MutableList<Medicine>? = null,
    val error: String? = null
)

suspend fun getMedicineListFromServer(context: Context): MedicineListResult {
    return withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            val client = OkHttpClient.Builder().build()

            val httpUrl: HttpUrl? = (Constants.BASE_URL + "/medicines").toHttpUrlOrNull()
            if (httpUrl == null) {
                cont.resume(
                    MedicineListResult(
                        error = context.resources.getString(
                            R.string.medicines_failed
                        )
                    )
                )
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
                    cont.resume(
                        MedicineListResult(
                            error = context.resources.getString(
                                R.string.medicines_failed
                            )
                        )
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    val message: String = response.body!!.string()
                    val gson = Gson()
                    if (response.code == 200) {
                        val medList =
                            gson.fromJson(message, Array<Medicine>::class.java)
                                .toMutableList()
                        cont.resume(MedicineListResult(success = medList))
                    } else {
                        onFailure(call, IOException(message))
                    }
                }
            })
        }
    }
}

suspend fun deleteMedicine(user_id: String, medicine_id: String): String? {
    return withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            val client = OkHttpClient.Builder().build()

            val httpUrl: HttpUrl? =
                (Constants.BASE_URL + "/user/delete_medicine").toHttpUrlOrNull()
            if (httpUrl == null) {
                cont.resume("Fail to build URL for server calling")
                return@suspendCoroutine
            }
            val httpUrlBuilder: HttpUrl.Builder = httpUrl.newBuilder()

            val jsonObject = JSONObject();
            jsonObject.put("user_id", user_id)
            jsonObject.put("medicine_id", medicine_id)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonObject.toString().toRequestBody(mediaType)

            lateinit var request: Request
            try {
                request = Request.Builder()
                    .url(httpUrlBuilder.build())
                    .delete(body)
                    .build()
            } catch (e: IllegalArgumentException) {
                cont.resume(e.message!!)
                return@suspendCoroutine
            }

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resume(e.message!!)
                }

                override fun onResponse(call: Call, response: Response) {
                    val message: String = response.body!!.string()
                    if (response.code != 200)
                        onFailure(call, IOException(message))
                    else {
                        cont.resume(null)
                    }
                }
            })
        }
    }
}