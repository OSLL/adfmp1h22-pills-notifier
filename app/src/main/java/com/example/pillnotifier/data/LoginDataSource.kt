package com.example.pillnotifier.data

import android.util.Log
import com.example.pillnotifier.Constants
import com.example.pillnotifier.data.model.LoggedInUser
import com.example.pillnotifier.data.model.UserInfo
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return suspendCoroutine { cont ->
            val user = UserInfo(null, username, password, null)
            val gson = Gson()
            val userJson = gson.toJson(user)
            val client = OkHttpClient.Builder().build()

            val body: RequestBody = userJson.toRequestBody("application/json".toMediaTypeOrNull())

            val request: Request = Request.Builder()
                .url(Constants.BASE_URL + "/user/login")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resume(Result.Error(e))
                }
                override fun onResponse(call: Call, response: Response) {
                    val message: String = response.body!!.string()
                    if (response.code == 200) {
                        val loggedInUser = gson.fromJson(message, LoggedInUser::class.java)
                        cont.resume(Result.Success(loggedInUser))
                    } else {
                        onFailure(call, IOException(message))
                    }
                }
            })
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}