package com.example.pillnotifier

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pillnotifier.fragments.DatePickerFragment
import com.example.pillnotifier.fragments.TimePickerFragment
import com.example.pillnotifier.model.*
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
import java.lang.RuntimeException
import java.time.LocalDate
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

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(call: Call, response: Response) {
                        val message: String = response.body!!.string()
                        if (response.code != 200)
                            onFailure(call, IOException(message))
                        else {
                            cachingNewMedicine(
                                this@MedicineProfile, Medicine(
                                    message,
                                    medicineInput.text.toString(),
                                    portionInput.text.toString(),
                                    takeTime.text.toString(),
                                    Regularity.valueOf(regularitySpinner.selectedItem.toString()),
                                    startDate.text.toString(),
                                    endDate.text.toString()
                                )
                            )
                            cont.resume(null)
                        }
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

                lateinit var request: Request
                try {
                    request = Request.Builder()
                        .url(httpUrlBuilder.build())
                        .put(body)
                        .build()
                } catch (e: IllegalArgumentException) {
                    cont.resume(e.message!!)
                    return@suspendCoroutine
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        cont.resume(e.message!!)
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(call: Call, response: Response) {
                        val message: String = response.body!!.string()
                        if (response.code != 200)
                            onFailure(call, IOException(message))
                        else {
                            val oldMedList =
                                getCachedMedicineList(this@MedicineProfile).toMutableList()
                            val medIndex = oldMedList.indexOfLast { it.medicine_id == medicineId }
                            if (medIndex == -1) {
                                onFailure(call, IOException("Didn't found medicine in cached list"))
                            } else {
                                oldMedList[medIndex] = Medicine(
                                    message,
                                    medicineInput.text.toString(),
                                    portionInput.text.toString(),
                                    takeTime.text.toString(),
                                    Regularity.valueOf(regularitySpinner.selectedItem.toString()),
                                    startDate.text.toString(),
                                    endDate.text.toString()
                                )
                                cachingMedicineList(this@MedicineProfile, oldMedList)
                                cont.resume(null)
                            }
                        }
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
            startDate.addTextChangedListener(object : TextWatcher {
                lateinit var prevStartDateText: String
                var counter = 0
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (counter == 0) {
                        prevStartDateText = p0.toString()
                    }
                    counter++
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    if (counter == 1 && !endDate.text.startsWith("Select") && startDate.text != prevStartDateText) {
                        val start = LocalDate.parse(
                            startDate.text,
                            DatePickerFragment.dateFormat
                        )
                        val end = LocalDate.parse(
                            endDate.text,
                            DatePickerFragment.dateFormat
                        )
                        if (start > end) {
                            startDate.text = prevStartDateText
                            Toast.makeText(
                                this@MedicineProfile,
                                "Start mustn't be later than end",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    counter--
                }
            })

            endDate.setOnClickListener {
                val newFragment = DatePickerFragment(endDate)
                newFragment.show(supportFragmentManager, "End date picker")
            }
            endDate.addTextChangedListener(object : TextWatcher {
                lateinit var prevEndDataText: String
                var counter = 0
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (counter == 0) {
                        prevEndDataText = p0.toString()
                    }
                    counter++
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    if (counter == 1 && !startDate.text.startsWith("Select") && endDate.text != prevEndDataText) {
                        val start = LocalDate.parse(
                            startDate.text,
                            DatePickerFragment.dateFormat
                        )
                        val end = LocalDate.parse(
                            endDate.text,
                            DatePickerFragment.dateFormat
                        )
                        if (end < start) {
                            endDate.text = prevEndDataText
                            Toast.makeText(
                                this@MedicineProfile,
                                "End mustn't be earlier than start",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    counter--
                }
            })

            takeTime.setOnClickListener {
                val newFragment = TimePickerFragment(takeTime)
                newFragment.show(supportFragmentManager, "Time picker")
            }
        }


        val curMedicine: Medicine? = intent.extras?.get("medicine") as Medicine?
        if (mode != Mode.CREATE) {
            if (curMedicine == null)
                throw RuntimeException("Medicine wasn't passed")
            medicineInput.setText(curMedicine!!.medicine_name ?: "")
            portionInput.setText(curMedicine!!.portion ?: "")
            regularitySpinner.setSelection(myAdapter.getPosition(curMedicine!!.regularity))
            startDate.text = curMedicine!!.start_date
            endDate.text = curMedicine!!.end_date
            takeTime.text = curMedicine!!.time

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
                        if (curMedicine == null)
                            throw RuntimeException("Medicine wasn't passed")
                        if (curMedicine!!.medicine_id == null)
                            throw RuntimeException("Medicine doesn't contains id")
                        else {
                            loading.visibility = View.VISIBLE
                            editMedicine(curMedicine!!.medicine_id!!)
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