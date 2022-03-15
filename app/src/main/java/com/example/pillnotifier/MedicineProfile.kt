package com.example.pillnotifier

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pillnotifier.fragments.DatePickerFragment
import com.example.pillnotifier.fragments.TimePickerFragment
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.Regularity
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


class MedicineProfile : AppCompatActivity() {
    enum class Mode {
        CREATE,
        EDIT,
        READ
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_profile)

        val medicineInput = findViewById<View>(R.id.input_medicine_name) as EditText
        val portionInput = findViewById<View>(R.id.input_medicine_portion) as EditText
        val submitButton = findViewById<View>(R.id.submitButton2) as Button
        val regularitySpinner = findViewById<View>(R.id.spinner1) as Spinner
        val startDate = findViewById<TextView>(R.id.tvStartDate)
        val endDate = findViewById<TextView>(R.id.tvEndDate)
        val takeTime = findViewById<TextView>(R.id.tvTakeTime)
        val loading = findViewById<ProgressBar>(R.id.loading)

        val mode: Mode = intent.extras?.get("mode") as Mode? ?: Mode.READ
        if (mode != Mode.READ) {
            val myAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, Regularity.values()
            )
            myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            regularitySpinner.adapter = myAdapter

            startDate.setOnClickListener {
                val newFragment = DatePickerFragment(startDate)
                newFragment.show(supportFragmentManager, "Start date picker")
            }

            endDate.setOnClickListener {
                val newFragment = DatePickerFragment(endDate)
                newFragment.show(supportFragmentManager, "End date picker")
            }

            takeTime.setOnClickListener {
                val newFragment = TimePickerFragment(takeTime)
                newFragment.show(supportFragmentManager, "Time picker")
            }
        }

        if (mode != Mode.CREATE) {
            // TODO set fields by medicine profile
            if (mode == Mode.READ) {
                for (view in listOf<View>(medicineInput, portionInput, regularitySpinner, startDate,
                    endDate, takeTime)) {
                    view.focusable = View.NOT_FOCUSABLE
                }
                submitButton.visibility = View.GONE
                return
            }
        }

        submitButton.setOnClickListener {
            when (mode) {
                Mode.CREATE -> {
                    // TODO call of server
                    loading.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        val errorMsg: String? = withContext(Dispatchers.IO) {
                            suspendCoroutine { cont ->
                                if (medicineInput.text.toString().isEmpty()) {
                                    cont.resume("Medicine name is empty")
                                    return@suspendCoroutine
                                }
                                if (!startDate.text.toString()[0].isDigit()) {
                                    cont.resume("Start date isn't selected")
                                    return@suspendCoroutine
                                }
                                if (!endDate.text.toString()[0].isDigit()) {
                                    cont.resume("End date isn't selected")
                                    return@suspendCoroutine
                                }
                                if (!takeTime.text.toString()[0].isDigit()) {
                                    cont.resume("Take time isn't selected")
                                    return@suspendCoroutine
                                }

                                val client = OkHttpClient.Builder().build()

                                val httpUrl: HttpUrl? = (Constants.BASE_URL + "/add_medicine").toHttpUrlOrNull()
                                if (httpUrl == null) {
                                    cont.resume("Fail to build URL for server calling")
                                    return@suspendCoroutine
                                }
                                val httpUrlBuilder: HttpUrl.Builder = httpUrl.newBuilder()

                                val jsonObject = JSONObject();
                                jsonObject.put("user_id", DataHolder.getData("userId"))
                                jsonObject.put("medicine_name", medicineInput.text.toString())
                                jsonObject.put("portion", portionInput.text.toString())
                                jsonObject.put("regularity", regularitySpinner.selectedItem.toString())
                                jsonObject.put("start_date", startDate.text.toString())
                                jsonObject.put("end_date", endDate.text.toString())
                                jsonObject.put("time", takeTime.text.toString())

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
                        if (errorMsg == null) {
                            onBackPressed()
                        } else {
                            Toast.makeText(this@MedicineProfile, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Mode.EDIT -> {
                    // TODO call on server
                    throw NotImplementedError()
                }
                Mode.READ -> throw IllegalAccessException("Submit button can't be visible if $mode mode")
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}