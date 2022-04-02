package com.example.pillnotifier.adapters.holders

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.pillnotifier.Constants
import com.example.pillnotifier.R
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RemovableProfileHolder(
    itemView: View, private val lifecycleScope: LifecycleCoroutineScope,
    private val context: Context?,
    private val url: String,
    private val updateFunc: () -> Unit,
) : AbstractProfileViewHolder(itemView) {
    private val removeButton: Button = itemView.findViewById(R.id.remove_button)
    private val userNameTV: TextView
    private val userNicknameTV: TextView

    init {
        val userItemView = itemView.findViewById<View>(R.id.user_item_include)
        userNameTV = userItemView.findViewById(R.id.user_name_tv)
        userNicknameTV = itemView.findViewById(R.id.user_nickname_tv)
    }

    override fun onBind(profile: Profile) {
        userNameTV.text = profile.name
        userNicknameTV.text = profile.nickname
        removeButton.setOnClickListener {
            lifecycleScope.launch {
                val errorMsg: String? = withContext(Dispatchers.IO) {
                    sendDependentRequest()
                }
                if (errorMsg != null) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "User ${userNicknameTV.text} removed",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateFunc()
                }
            }
        }
    }

    private suspend fun sendDependentRequest() : String? = suspendCoroutine { cont ->
        if (userNicknameTV.text.toString().isEmpty()) {
            cont.resume("Username is empty")
            return@suspendCoroutine
        }
        val client = OkHttpClient.Builder().build()

        val httpUrl: HttpUrl? =
            (Constants.BASE_URL + url).toHttpUrlOrNull()
        if (httpUrl == null) {
            cont.resume("Fail to build URL for server calling")
            return@suspendCoroutine
        }
        val httpUrlBuilder: HttpUrl.Builder = httpUrl.newBuilder()

        val jsonObject = JSONObject()
        jsonObject.put("user_id", DataHolder.getData("userId"))
        jsonObject.put("username", userNicknameTV.text.toString())

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonObject.toString().toRequestBody(mediaType)

        val request: Request = Request.Builder()
            .url(httpUrlBuilder.build())
            .post(body)
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