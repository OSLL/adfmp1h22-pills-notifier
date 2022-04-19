package com.example.pillnotifier.model

data class NotificationResult(
    val success: MutableList<Notification>? = null,
    val error: Int? = null
)