package com.example.pillnotifier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.holders.AbstractProfileViewHolder
import com.example.pillnotifier.model.Profile

typealias ProfileAdapterCreator<ViewHolderType> =  (Context, List<Profile>) -> ProfileAdapter<ViewHolderType>

class ProfileAdapter<ViewHolderType : AbstractProfileViewHolder>(
    private val context: Context,
    private val profiles: List<Profile>,
    private val profile_item_layout_id: Int,
    private val holderCreator: (View) -> ViewHolderType
) : RecyclerView.Adapter<ViewHolderType>() {
    private val layoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderType {
        val view: View = layoutInflater.inflate(profile_item_layout_id, parent, false)
        return holderCreator(view)
    }

    override fun onBindViewHolder(holder: ViewHolderType, position: Int) {
        val profile = profiles[position]
        holder.onBind(profile)
    }

    override fun getItemCount(): Int = profiles.size
}