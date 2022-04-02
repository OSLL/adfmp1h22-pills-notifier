package com.example.pillnotifier.model

data class ScheduleResult(
    val success: MutableList<MedicineTake>? = null,
    val error: String? = null
)