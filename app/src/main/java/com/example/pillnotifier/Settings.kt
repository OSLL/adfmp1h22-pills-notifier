package com.example.pillnotifier

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pillnotifier.data.Result
import com.example.pillnotifier.model.UserInfo
import com.example.pillnotifier.model.DataHolder
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.IllegalArgumentException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class Settings : AppCompatActivity() {
    var name: String? = null
    private var link: String? = null

    private var nameInput: EditText? = null
    private var linkInput: EditText? = null

    private var submitButton: Button? = null

    private suspend fun _updateUserInfo(
        fullname: String?,
        username: String?
    ): Result<String> {
        return suspendCoroutine { cont ->
            val user = UserInfo(fullname, username, null, DataHolder.getData("userId"))
            val gson = Gson()
            val userJson = gson.toJson(user)
            val client = OkHttpClient.Builder().build()

            val body: RequestBody = userJson.toRequestBody("application/json".toMediaTypeOrNull())

            lateinit var request: Request
            try {
                request = Request.Builder()
                .url(Constants.BASE_URL + "/user/update")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()
            } catch (e: IllegalArgumentException) {
                cont.resume(Result.Error(e))
                return@suspendCoroutine
            }
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resume(Result.Error(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val message: String = response.body!!.string()
                    if (response.code == 200) {
                        cont.resume(Result.Success(message))
                    } else {
                        onFailure(call, IOException(message))
                    }
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        nameInput = findViewById<View>(R.id.name) as EditText
        linkInput = findViewById<View>(R.id.link_edit) as EditText
        submitButton = findViewById<View>(R.id.submitButton) as Button
        submitButton!!.setOnClickListener {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    name = nameInput?.text.toString()
                    if (name == null || name == "") {
                        name = DataHolder.getData("fullname")
                    }

                    link = linkInput?.text.toString()
                    if (link == null || link == "") {
                        link = DataHolder.getData("username")
                    }
                    _updateUserInfo(name, link)
                }
                if (result is Result.Success) {
                    Toast.makeText(
                        applicationContext,
                        "User profile updated",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Could not update user profile",
                        Toast.LENGTH_LONG
                    ).show()
                }
                val intent = Intent()
                name = nameInput?.text.toString()
                if (name != null && name != "") {
                    intent.putExtra("username", name)
                }
                link = linkInput?.text.toString()
                if (link != null && link != "") {
                    intent.putExtra("link", link)
                }
                setResult(Activity.RESULT_OK, intent)
                onBackPressed()
            }
        }
    }
}