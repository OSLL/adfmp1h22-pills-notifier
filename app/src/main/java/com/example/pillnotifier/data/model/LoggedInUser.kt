package com.example.pillnotifier.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val fullname: String,
    val username: String
)