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
import com.example.pillnotifier.adapters.holders.AbstractProfileViewHolder
import com.example.pillnotifier.model.ProfilesList

class ProfilesListAdapter<ViewHolderType : AbstractProfileViewHolder>(
    private val context: Context,
    private val profilesListsWithAdapterCreators: List<Pair<ProfilesList, ProfileAdapterCreator<ViewHolderType>>>,
) : RecyclerView.Adapter<ProfilesListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfilesListAdapter.ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.profiles_list, parent, false)
        return ProfilesListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profilesListWithAdapterCreator = profilesListsWithAdapterCreators[position]

        holder.listNameTV.text = profilesListWithAdapterCreator.first.list_name
        holder.listSizeTV.text =
            profilesListWithAdapterCreator.first.profiles.size.toString() + " people"

        holder.profilesListRV.adapter = profilesListWithAdapterCreator.second(
            context,
            profilesListWithAdapterCreator.first.profiles

        )
        holder.profilesListRV.addItemDecoration(
            DividerItemDecoration(
                holder.profilesListRV.context,
                DividerItemDecoration.VERTICAL
            )
        )

        holder.showMedsIV.setOnClickListener(object : View.OnClickListener {
            private var isScheduleGone = true
            override fun onClick(p0: View?) {
                if (isScheduleGone) {
                    holder.profilesListRV.visibility = View.VISIBLE
                    holder.showMedsIV.setImageResource(R.drawable.close_schedule)
                } else {
                    holder.profilesListRV.visibility = View.GONE
                    holder.showMedsIV.setImageResource(R.drawable.open_schedule)
                }
                isScheduleGone = !isScheduleGone
            }
        })
    }

    override fun getItemCount(): Int = profilesListsWithAdapterCreators.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listNameTV: TextView = itemView.findViewById(R.id.list_name_tv)
        val listSizeTV: TextView = itemView.findViewById(R.id.list_size_tv)
        val showMedsIV: ImageView = itemView.findViewById(R.id.show_meds_iv)
        val profilesListRV: RecyclerView = itemView.findViewById(R.id.profiles_lists_rv)
    }
}