package com.example.pillnotifier.data

import com.example.pillnotifier.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

            val request: Request = Request.Builder()
                .url(httpUrlBuilder.build())
                .delete(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resume(e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    val message: String = response.body!!.string()
                    if (response.code != 200)
                        onFailure(call, IOException(message))
                    else
                        cont.resume(null)
                }
            })
        }
    }
}