package com.example.pillnotifier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.model.DependentWithTakes

class DependentWithTakesAdapter(
    private val context: Context,
    private val dependentsWithTakes: List<DependentWithTakes>
) : RecyclerView.Adapter<DependentWithTakesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.dependent_item, parent, false)
        return DependentWithTakesAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dependentWithTakes = dependentsWithTakes[position]

        holder.userNameTV.text = dependentWithTakes.dependentProfile.name
        holder.userNicknameTV.text = "@" + dependentWithTakes.dependentProfile.nickname

        holder.dependentScheduleRV.adapter =
            MedicineTakeAdapter(context, dependentWithTakes.medicineTakes, Rights.READ)
        holder.dependentScheduleRV.addItemDecoration(
            DividerItemDecoration(
                holder.dependentScheduleRV.context,
                DividerItemDecoration.VERTICAL
            )
        )

        holder.showMedsIV.setOnClickListener(object : View.OnClickListener {
            private var isScheduleGone = true
            override fun onClick(p0: View?) {
                if (isScheduleGone) {
                    holder.dependentScheduleRV.visibility = View.VISIBLE
                    holder.showMedsIV.setImageResource(R.drawable.close_schedule)
                } else {
                    holder.dependentScheduleRV.visibility = View.GONE
                    holder.showMedsIV.setImageResource(R.drawable.open_schedule)
                }
                isScheduleGone = !isScheduleGone
            }
        })
    }

    override fun getItemCount(): Int = dependentsWithTakes.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTV: TextView
        val userNicknameTV: TextView
        val showMedsIV: ImageView = itemView.findViewById(R.id.show_meds_iv)
        val dependentScheduleRV: RecyclerView = itemView.findViewById(R.id.dependents_schedule_rv)

        init {
            val userItemView = itemView.findViewById<View>(R.id.user_item_include)
            userNameTV = userItemView.findViewById(R.id.user_name_tv)
            userNicknameTV = itemView.findViewById(R.id.user_nickname_tv)
        }
    }
}