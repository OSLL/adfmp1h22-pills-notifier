package com.example.pillnotifier.model

enum class TakeStatus {
    TAKEN,
    NOT_TAKEN,
    UNKNOWN
}

class MedicineTake(val medicine: Medicine, var take_status: TakeStatus, val date: String)
