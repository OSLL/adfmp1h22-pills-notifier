package com.example.pillnotifier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.model.MedicineTake
import java.text.SimpleDateFormat
import java.util.*

class MedicineAdapter(context: Context, private val medicines: List<MedicineTake>) : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = layoutInflater.inflate(R.layout.medicine_in_schedule_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicineTake = medicines.get(position)
        holder.medicineNameTV.text = medicineTake.medicine.name
        holder.portionTV.text = medicineTake.medicine.portion

        val df = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.takeTimeTV.text = df.format(medicineTake.dateOfTake)

        // TODO: reactions on clicking taking and not taking
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