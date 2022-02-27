package com.example.pillnotifier.data.model

import com.google.gson.annotations.SerializedName

data class UserInfo (
    @SerializedName("username")
    val username: String?,
    @SerializedName("password")
    val password: String?
)