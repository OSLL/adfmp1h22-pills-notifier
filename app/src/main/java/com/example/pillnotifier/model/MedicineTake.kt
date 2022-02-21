package com.example.pillnotifier.model;

import java.time.LocalDate

enum class TakeStatus {
    TAKEN,
    NOT_TAKEN,
    UNKNOWN
}

class MedicineTake(val medicine: Medicine, val takeStatus: TakeStatus)
