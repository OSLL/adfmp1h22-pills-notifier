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
import java.lang.IllegalArgumentException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IncomingProfileHolder(
    itemView: View, private val lifecycleScope: LifecycleCoroutineScope,
    private val context: Context?,
    private val updateFunc: () -> Unit,
) : AbstractProfileViewHolder(itemView) {
    private val declineButton: Button = itemView.findViewById(R.id.decline_button)
    private val acceptButton: Button = itemView.findViewById(R.id.accept_button)
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
        declineButton.setOnClickListener {
            lifecycleScope.launch {
                val errorMsg: String? = withContext(Dispatchers.IO) {
                    sendIncomingRequest("/incoming/decline")
                }
                if (errorMsg != null) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Request from ${userNicknameTV.text} declined",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateFunc()
                }
            }
        }

        acceptButton.setOnClickListener {
            lifecycleScope.launch {
                val errorMsg: String? = withContext(Dispatchers.IO) {
                    sendIncomingRequest("/incoming/accept")
                }
                if (errorMsg != null) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Request from ${userNicknameTV.text} accepted",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateFunc()
                }
            }
        }
    }

    private suspend fun sendIncomingRequest(url: String) : String? = suspendCoroutine { cont ->
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

        lateinit var request: Request
        try {
            request = Request.Builder()
            .url(httpUrlBuilder.build())
            .post(body)
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
                else
                    cont.resume(null)
            }
        })
    }
}