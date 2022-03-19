package com.example.pillnotifier.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.MedicineProfile
import com.example.pillnotifier.R
import com.example.pillnotifier.data.deleteMedicine
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.Medicine
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MedicineAdapter(
    private val context: Context,
    private val medicines: MutableList<Medicine>,
    private val activityForResultLauncher: ActivityResultLauncher<Intent>
) : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = layoutInflater.inflate(R.layout.medicine_in_medicine_item, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.medicineName.text = medicine.medicine_name
        holder.portion.text = medicine.portion
        holder.regularityAndTakeTime.text = medicine.regularity!!.stringInterpretation +
                " at " + medicine.time!!.format(DateTimeFormatter.ofPattern("HH:mm"))
        holder.deleteButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            var errorMsg: String? = null
                            GlobalScope.launch {
                                errorMsg = deleteMedicine(DataHolder.getData("userId"), medicine.medicine_id!!)
                            }
                            if (errorMsg != null) {
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            } else {
                                medicines.removeAt(position)
                                notifyDataSetChanged()
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
        }
        holder.editButton.setOnClickListener {
            val intent = Intent(it.context, MedicineProfile::class.java)
            intent.putExtra("mode", MedicineProfile.Mode.EDIT)
            intent.putExtra("medicine", medicine)
            activityForResultLauncher.launch(intent)
        }
    }

    override fun getItemCount(): Int {
        return medicines.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
        val portion: TextView = itemView.findViewById(R.id.medicine_portion)
        val regularityAndTakeTime: TextView = itemView.findViewById(R.id.regularity_ant_take_time)
        val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete)
        val editButton: ImageButton = itemView.findViewById(R.id.button_edit)
    }
}