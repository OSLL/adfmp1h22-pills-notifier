package com.example.pillnotifier.adapters.holders

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.pillnotifier.R
import com.example.pillnotifier.model.Profile

class RemovableProfileHolder(itemView: View) : AbstractProfileViewHolder(itemView) {
    private val removeButton: Button = itemView.findViewById(R.id.remove_button)
    private val userNameTV: TextView
    private val userNicknameTV: TextView

    init {
        val userItemView = itemView.findViewById<View>(R.id.user_item_include)
        userNameTV = userItemView.findViewById(R.id.user_name_tv)
        userNicknameTV = itemView.findViewById(R.id.user_nickname_tv)
    }

    override fun onBind(profile: Profile) {
        userNameTV.text = profile.name
        userNicknameTV.text = profile.nickname
    }
}