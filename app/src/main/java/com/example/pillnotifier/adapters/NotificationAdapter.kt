package com.example.pillnotifier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(context: Context, private val notifications: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = layoutInflater.inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.notification_message.text = notification.message
        holder.notification_date.text = notification.date
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notification_message: TextView = itemView.findViewById(R.id.message)
        val notification_date: TextView = itemView.findViewById(R.id.date)
    }
}