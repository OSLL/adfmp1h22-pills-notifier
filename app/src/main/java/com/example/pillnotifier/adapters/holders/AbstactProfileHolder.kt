package com.example.pillnotifier.adapters.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.model.Profile

abstract class AbstractProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    abstract fun onBind(profile: Profile)
}