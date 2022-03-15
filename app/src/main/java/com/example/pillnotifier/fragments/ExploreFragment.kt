package com.example.pillnotifier.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.Constants
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.ProfileAdapter
import com.example.pillnotifier.adapters.ProfileAdapterCreator
import com.example.pillnotifier.adapters.ProfilesListAdapter
import com.example.pillnotifier.adapters.holders.AbstractProfileViewHolder
import com.example.pillnotifier.adapters.holders.SimpleProfileHolder
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.ProfilesList
import com.google.gson.Gson
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

class ExploreFragment : Fragment() {

    // TODO in the future change SimpleProfileHolder to other implementations of AbstractProfileViewHolder
    private val profilesListWithAdaptCreators =
        mutableListOf<Pair<ProfilesList, ProfileAdapterCreator<AbstractProfileViewHolder>>>()

    private class ExploreListResult(
        val success: MutableList<ProfilesList>? = null,
        val error: Int? = null
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        val exploreListLL: LinearLayout = view.findViewById(R.id.explore_list)

        val recyclerView: RecyclerView = view.findViewById(R.id.profiles_lists_rv)
        val loading = view.findViewById<ProgressBar>(R.id.loading)
        recyclerView.adapter = ProfilesListAdapter(requireContext(), profilesListWithAdaptCreators)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        exploreListLL.visibility = View.GONE
        loading.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result: ExploreListResult = withContext(Dispatchers.IO) {
                suspendCoroutine { cont ->
                    val client = OkHttpClient.Builder().build()

                    val httpUrl: HttpUrl? = (Constants.BASE_URL + "/explore").toHttpUrlOrNull()
                    if (httpUrl == null) {
                        cont.resume(ExploreListResult(error = R.string.explore_failed))
                    }

                    val httpUrlBuilder: HttpUrl.Builder = httpUrl!!.newBuilder()
                    httpUrlBuilder.addQueryParameter("user_id", DataHolder.getData("userId"))

                    val request: Request = Request.Builder()
                        .url(httpUrlBuilder.build())
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            cont.resume(ExploreListResult(error = R.string.explore_failed))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val message: String = response.body!!.string()
                            val gson = Gson()
                            if (response.code == 200) {
                                val expList =
                                    gson.fromJson(message, Array<ProfilesList>::class.java)
                                        .toMutableList()
                                cont.resume(ExploreListResult(success = expList))
                            } else {
                                onFailure(call, IOException(message))
                            }
                        }
                    })
                }
            }
            if (result.success != null) {
                profilesListWithAdaptCreators.addAll(
                    listOf(
                        Pair(result.success[0]) { c, pl ->
                            ProfileAdapter(
                                c,
                                pl,
                                R.layout.removable_user_list_item
                            ) { v -> SimpleProfileHolder(v) }
                        },

                        Pair(result.success[1]) { c, pl ->
                            ProfileAdapter(
                                c,
                                pl,
                                R.layout.removable_user_list_item
                            ) { v -> SimpleProfileHolder(v) }
                        },

                        Pair(result.success[2]) { c, pl ->
                            ProfileAdapter(
                                c,
                                pl,
                                R.layout.incoming_request_item
                            ) { v -> SimpleProfileHolder(v) }
                        },

                        Pair(result.success[3]) { c, pl ->
                            ProfileAdapter(
                                c,
                                pl,
                                R.layout.outgoing_request_item
                            ) { v -> SimpleProfileHolder(v) }
                        },
                    )
                )
                recyclerView.adapter!!.notifyDataSetChanged()
            } else if (result.error != null) {
                Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
            }
            loading.visibility = View.GONE
            exploreListLL.visibility = View.VISIBLE
        }

        val dependentInput: EditText = view.findViewById(R.id.dependent_input)
        val searchIV: ImageView = view.findViewById(R.id.search_iv)
        searchIV.setOnClickListener {
            loading.visibility = View.VISIBLE
            lifecycleScope.launch {
                val errorMsg: String? = withContext(Dispatchers.IO) {
                    suspendCoroutine { cont ->
                        if (dependentInput.text.toString().isEmpty()) {
                            cont.resume("Dependent username is empty")
                            return@suspendCoroutine
                        }
                        val client = OkHttpClient.Builder().build()

                        val httpUrl: HttpUrl? =
                            (Constants.BASE_URL + "/dependent/send").toHttpUrlOrNull()
                        if (httpUrl == null) {
                            cont.resume("Fail to build URL for server calling")
                            return@suspendCoroutine
                        }
                        val httpUrlBuilder: HttpUrl.Builder = httpUrl.newBuilder()

                        val jsonObject = JSONObject()
                        jsonObject.put("user_id", DataHolder.getData("userId"))
                        jsonObject.put("dependent_username", dependentInput.text.toString())

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

                loading.visibility = View.GONE

                if (errorMsg != null) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Request to user ${dependentInput.text} was sent",
                        Toast.LENGTH_SHORT
                    ).show()
                    dependentInput.text.clear()
                }
            }
        }


        return view
    }

}