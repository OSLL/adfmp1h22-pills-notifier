package com.example.pillnotifier.model

enum class TakeStatus {
    TAKEN,
    NOT_TAKEN,
    UNKNOWN
}

class MedicineTake(val medicine: Medicine, val take_status: TakeStatus)
