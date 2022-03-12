package com.example.pillnotifier.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.model.MedicineTake
import com.example.pillnotifier.model.TakeStatus
import java.time.format.DateTimeFormatter

enum class Rights {
    READ,
    WRITE
}

class MedicineTakeAdapter (
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

        holder.takeTimeTV.text = medicineTake.medicine.time!!.format(DateTimeFormatter.ofPattern("HH:mm"))

        holder.itemView.setBackgroundColor(context.resources.getColor(R.color.white))
        holder.takenIV.visibility = View.VISIBLE
        holder.notTakenIV.visibility = View.VISIBLE
        when (medicineTake.take_status) {
            TakeStatus.UNKNOWN -> {
                holder.itemView.setBackgroundColor(context.resources.getColor(R.color.unknown_status))
                if (rights == Rights.READ) {
                        holder.takenIV.visibility = View.INVISIBLE
                        holder.notTakenIV.visibility = View.INVISIBLE
                }
            }
            TakeStatus.TAKEN -> holder.notTakenIV.visibility = View.INVISIBLE
            TakeStatus.NOT_TAKEN -> holder.takenIV.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return medicines.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val takenIV: ImageView = itemView.findViewById(R.id.taken_iv)
        val notTakenIV: ImageView = itemView.findViewById(R.id.not_taken_iv)
        val takeTimeTV: TextView = itemView.findViewById(R.id.take_time_tv)
        val medicineNameTV: TextView = itemView.findViewById(R.id.medicine_name_tv)
        val portionTV: TextView = itemView.findViewById(R.id.portion_tv)
    }
}