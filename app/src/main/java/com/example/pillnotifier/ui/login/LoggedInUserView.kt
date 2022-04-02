package com.example.pillnotifier.ui.login

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val userId: String,
    val fullname: String,
    val username: String
)