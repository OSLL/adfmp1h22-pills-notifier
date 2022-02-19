package com.example.pillnotifier.adapters.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.pillnotifier.R
import com.example.pillnotifier.model.Profile

class SimpleProfileHolder(itemView: View) : AbstractProfileViewHolder(itemView) {
    private val userNameTV: TextView
    private val userNicknameTV: TextView

    init {
        val userItemView = itemView.findViewById<View>(R.id.user_item_include)
        userNameTV = userItemView.findViewById(R.id.user_name_tv)
        userNicknameTV = itemView.findViewById(R.id.user_nickname_tv)
    }

    override fun onBind(profile: Profile) {
        userNameTV.text = profile.name
        userNicknameTV.text = "@" + profile.nickname
    }
}