package com.example.pillnotifier

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pillnotifier.data.deleteMedicine
import com.example.pillnotifier.fragments.DatePickerFragment
import com.example.pillnotifier.fragments.TimePickerFragment
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.Medicine
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
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MedicineProfile : AppCompatActivity() {
    enum class Mode {
        CREATE,
        EDIT,
        READ
    }

    private suspend fun addMedicine() {
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

        if (errorMsg == null) {
            setResult(RESULT_OK)
            onBackPressed()
        } else {
            Toast.makeText(this@MedicineProfile, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun editMedicine(medicineId: String) {
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

                val httpUrl: HttpUrl? = (Constants.BASE_URL + "/edit_medicine").toHttpUrlOrNull()
                if (httpUrl == null) {
                    cont.resume("Fail to build URL for server calling")
                    return@suspendCoroutine
                }
                val httpUrlBuilder: HttpUrl.Builder = httpUrl.newBuilder()

                val jsonObject = JSONObject();
                jsonObject.put("medicine_id", medicineId)
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
                    .put(body)
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

        if (errorMsg == null) {
            setResult(RESULT_OK)
            onBackPressed()
        } else {
            Toast.makeText(this@MedicineProfile, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var medicineInput: EditText
    private lateinit var portionInput: EditText
    private lateinit var regularitySpinner: Spinner
    private lateinit var startDate: TextView
    private lateinit var endDate: TextView
    private lateinit var takeTime: TextView
    private lateinit var loading: ProgressBar

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_profile)

        medicineInput = findViewById<View>(R.id.input_medicine_name) as EditText
        portionInput = findViewById<View>(R.id.input_medicine_portion) as EditText
        regularitySpinner = findViewById<View>(R.id.regularity_spinner) as Spinner
        startDate = findViewById<TextView>(R.id.tvStartDate)
        endDate = findViewById<TextView>(R.id.tvEndDate)
        takeTime = findViewById<TextView>(R.id.tvTakeTime)
        loading = findViewById<ProgressBar>(R.id.loading)

        val submitButton = findViewById<View>(R.id.submitMedicineButton) as Button

        val mode: Mode = intent.extras?.get("mode") as Mode? ?: Mode.READ
        val myAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, Regularity.values()
        )
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        regularitySpinner.adapter = myAdapter
        if (mode != Mode.READ) {
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

        val medicine: Medicine? = intent.extras?.get("medicine") as Medicine?
        if (mode != Mode.CREATE) {
            if (medicine == null)
                throw RuntimeException("Medicine wasn't passed")
            medicineInput.setText(medicine.medicine_name ?: "")
            portionInput.setText(medicine.portion ?: "")
            regularitySpinner.setSelection(myAdapter.getPosition(medicine.regularity))
            startDate.text = medicine.start_date
            endDate.text = medicine.end_date
            takeTime.text = medicine.time

            if (mode == Mode.READ) {
                for (view in listOf(
                    medicineInput, portionInput, regularitySpinner, startDate,
                    endDate, takeTime
                )) {
                    view.focusable = View.NOT_FOCUSABLE
                }
                regularitySpinner.isEnabled = false
                submitButton.visibility = View.GONE
                return
            }
        }

        submitButton.setOnClickListener {
            when (mode) {
                Mode.CREATE -> {
                    lifecycleScope.launch {
                        loading.visibility = View.VISIBLE
                        addMedicine()
                        loading.visibility = View.GONE
                    }
                }
                Mode.EDIT -> {
                    lifecycleScope.launch {
                        if (medicine == null)
                            throw RuntimeException("Medicine wasn't passed")
                        if (medicine.medicine_id == null)
                            throw RuntimeException("Medicine doesn't contains id")
                        else {
                            loading.visibility = View.VISIBLE
                            editMedicine(medicine.medicine_id!!)
                            loading.visibility = View.GONE
                        }
                    }
                }
                Mode.READ -> throw IllegalAccessException("Submit button can't be visible if $mode mode")
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED)
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}