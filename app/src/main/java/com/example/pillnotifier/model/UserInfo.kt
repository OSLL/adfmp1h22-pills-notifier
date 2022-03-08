package com.example.pillnotifier.model

import com.google.gson.annotations.SerializedName

data class UserInfo (
    @SerializedName("full_name")
    val fullname: String?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("password")
    val password: String?,
    @SerializedName("user_id")
    val userId: String?
)