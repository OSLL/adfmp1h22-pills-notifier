package com.example.pillnotifier.adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.MedicineProfile
import com.example.pillnotifier.R
import com.example.pillnotifier.model.Medicine
import java.time.format.DateTimeFormatter


class MedicineAdapter(context: Context, private val medicine: List<Medicine>) : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = layoutInflater.inflate(R.layout.medicine_in_medicine_item, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicine.get(position)
        holder.medicineName.text = medicine.name
        holder.portion.text = medicine.instruction
        holder.regularityAndTakeTime.text = medicine.regularity.stringInterpretation +
                " at " + medicine.takeTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        holder.deleteButton.setOnClickListener {
            val intent = Intent(it.context, MedicineProfile::class.java)
            it.context.startActivity(intent)
        }
        holder.editButton.setOnClickListener {
            val intent = Intent(it.context, MedicineProfile::class.java)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return medicine.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
        val portion: TextView = itemView.findViewById(R.id.medicine_portion)
        val regularityAndTakeTime: TextView = itemView.findViewById(R.id.regularity_ant_take_time)
        val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete)
        val editButton: ImageButton = itemView.findViewById(R.id.button_edit)
    }
}