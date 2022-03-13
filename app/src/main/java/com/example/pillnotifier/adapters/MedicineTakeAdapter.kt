package com.example.pillnotifier.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.Constants
import com.example.pillnotifier.R
import com.example.pillnotifier.data.Result
import com.example.pillnotifier.data.model.LoggedInUser
import com.example.pillnotifier.model.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class Rights {
    READ,
    WRITE
}

class MedicineTakeAdapter(
    private val context: Context,
    private val medicines: List<MedicineTake>,
    private val rights: Rights
) :
    RecyclerView.Adapter<MedicineTakeAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = layoutInflater.inflate(R.layout.medicine_in_schedule_item, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicineTake = medicines[position]
        holder.medicineNameTV.text = medicineTake.medicine.medicine_name
        holder.portionTV.text = medicineTake.medicine.portion

        holder.takeTimeTV.text =
            medicineTake.medicine.time!!.format(DateTimeFormatter.ofPattern("HH:mm"))

        holder.itemView.setBackgroundColor(context.resources.getColor(R.color.white))
        holder.takenIV.visibility = View.VISIBLE
        holder.notTakenIV.visibility = View.VISIBLE
        when (medicineTake.take_status) {
            TakeStatus.UNKNOWN -> {
                holder.itemView.setBackgroundColor(context.resources.getColor(R.color.unknown_status))
                if (rights == Rights.READ) {
                    holder.takenIV.visibility = View.GONE
                    holder.notTakenIV.visibility = View.GONE
                    holder.notTakenIV.isClickable = false
                    holder.takenIV.isClickable = false
                } else {
                    holder.takenIV.setOnClickListener(View.OnClickListener {
                        updateStatus(
                            TakeStatus.TAKEN,
                            medicineTake.date,
                            medicineTake.medicine.medicine_id
                        )
                        holder.notTakenIV.visibility = View.GONE
                        medicineTake.take_status = TakeStatus.TAKEN
                        holder.notTakenIV.isClickable = false
                        holder.itemView.setBackgroundColor(context.resources.getColor(R.color.white))
                    })
                    holder.notTakenIV.setOnClickListener(View.OnClickListener {
                        updateStatus(
                            TakeStatus.NOT_TAKEN,
                            medicineTake.date,
                            medicineTake.medicine.medicine_id
                        )
                        holder.takenIV.visibility = View.GONE
                        medicineTake.take_status = TakeStatus.NOT_TAKEN
                        holder.notTakenIV.isClickable = false
                        holder.itemView.setBackgroundColor(context.resources.getColor(R.color.white))
                    })
                }
            }
            TakeStatus.TAKEN -> {
                holder.notTakenIV.visibility = View.GONE
                holder.notTakenIV.isClickable = false
                holder.takenIV.isClickable = false
            }
            TakeStatus.NOT_TAKEN -> {
                holder.takenIV.visibility = View.GONE
                holder.notTakenIV.isClickable = false
                holder.takenIV.isClickable = false
            }
        }
    }

    private fun showStatusUpdateFailed(@StringRes errorString: Int) {
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
    }

    fun updateStatus(status: TakeStatus, dateString: String, medicineId: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val result: RequestResult = withContext(Dispatchers.IO) {
                suspendCoroutine { cont ->
                    val jsonObject = JSONObject();
                    jsonObject.put("user_id", DataHolder.getData("userId"))
                    jsonObject.put("date", dateString)
                    jsonObject.put("medicine_id", medicineId)
                    jsonObject.put("medicine_status", status)

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = jsonObject.toString().toRequestBody(mediaType)
                    val client = OkHttpClient.Builder().build()

                    val request: Request = Request.Builder()
                        .url(Constants.BASE_URL + "/medicine/status")
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            cont.resume(RequestResult(error = R.string.status_update_failed))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val message: String = response.body!!.string()
                            if (response.code == 200) {
                                cont.resume(RequestResult(success = "OK"))
                            } else {
                                onFailure(call, IOException(message))
                            }
                        }
                    })
                }
            }
            if (result.error != null) {
                showStatusUpdateFailed(result.error)
            }
        }
    }

    override fun getItemCount(): Int {
        return medicines.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val takenIV: ImageButton = itemView.findViewById(R.id.taken_iv)
        val notTakenIV: ImageButton = itemView.findViewById(R.id.not_taken_iv)
        val takeTimeTV: TextView = itemView.findViewById(R.id.take_time_tv)
        val medicineNameTV: TextView = itemView.findViewById(R.id.medicine_name_tv)
        val portionTV: TextView = itemView.findViewById(R.id.portion_tv)
    }
}