package com.example.pillnotifier.data

import android.os.AsyncTask
import android.util.Log
import com.example.pillnotifier.Constants
import com.example.pillnotifier.data.model.LoggedInUser
import com.example.pillnotifier.data.model.LoginResponse
import com.example.pillnotifier.data.model.UserInfo
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return suspendCoroutine { cont ->
            val user = UserInfo(username, password)
            val gson = Gson()
            val userJson = gson.toJson(user)
            Log.d("MY_LOG", userJson)
            val client = OkHttpClient.Builder().build()

            val body: RequestBody = userJson.toRequestBody("application/json".toMediaTypeOrNull())
            Log.d("MY_LOG", "application/json".toMediaTypeOrNull().toString())

            val request: Request = Request.Builder()
                .url(Constants.BASE_URL + "/user/login")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("MY_LOG", "FAILURE ")
                    Log.d("MY_LOG", e.message.toString())
                    cont.resume(Result.Error(e))
                }
                override fun onResponse(call: Call, response: Response) {
                    val message: String = response.body!!.string()
                    if (response.code == 200) {
                        cont.resume(Result.Success(LoggedInUser(message, username)))
                    } else {
                        Log.d("MY_LOG", "RETURN CODE")
                        cont.resume(Result.Error(Exception(message)))
                    }
                }
            })
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}