package com.example.pillnotifier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
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
import com.example.pillnotifier.model.Profile
import com.example.pillnotifier.model.ProfilesList
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
        mutableListOf<Pair<ProfilesList, ProfileAdapterCreator<AbstractProfileViewHolder>>>(
            Pair(ProfilesList("Dependents", listOf(
                    Profile("Sarah Gallagher", "sgallagher"),
                ))){c, pl -> ProfileAdapter(c, pl, R.layout.removable_user_list_item){ v -> SimpleProfileHolder(v) } },

            Pair(ProfilesList("Observers", listOf(
                    Profile("Sarah Gallagher", "sgallagher"), Profile("Anna Smith", "asmith")
                ))){c, pl -> ProfileAdapter(c, pl, R.layout.removable_user_list_item){ v -> SimpleProfileHolder(v) } },

            Pair(ProfilesList("Incoming requests", listOf(
                    Profile("Kimberly White", "kwhite")
            ))){c, pl -> ProfileAdapter(c, pl, R.layout.incoming_request_item){ v -> SimpleProfileHolder(v) } },

            Pair(ProfilesList("Outgoing requests", listOf(
                Profile("Jane Thompson", "jthompson")
            ))){c, pl -> ProfileAdapter(c, pl, R.layout.outgoing_request_item){ v -> SimpleProfileHolder(v) } },
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.profiles_lists_rv)
        val loading = view.findViewById<ProgressBar>(R.id.loading)
        recyclerView.adapter = ProfilesListAdapter(requireContext(), profilesListWithAdaptCreators)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        val dependentInput: EditText = view.findViewById(R.id.dependent_input)
        val searchIV: ImageView = view.findViewById(R.id.search_iv)
        searchIV.setOnClickListener{ v ->
          //  Toast.makeText(context, "Not found user @${dependentInput.text}", Toast.LENGTH_LONG).show()
            loading.visibility = View.VISIBLE
            lifecycleScope.launch {
                val errorMsg: String? = withContext(Dispatchers.IO) {
                    suspendCoroutine { cont ->
                        if (dependentInput.text.toString().isEmpty()) {
                            cont.resume("Dependent username is empty")
                            return@suspendCoroutine
                        }
                        val client = OkHttpClient.Builder().build()

                        val httpUrl: HttpUrl? = (Constants.BASE_URL + "/dependent/send").toHttpUrlOrNull()
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
                    Toast.makeText(context, "Request to user ${dependentInput.text} was sent", Toast.LENGTH_SHORT).show()
                    dependentInput.text.clear()
                }
            }
        }


        return view
    }

}