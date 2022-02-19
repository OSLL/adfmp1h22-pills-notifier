package com.example.pillnotifier.model;

import java.util.Date

enum class TakeStatus {
    TAKEN,
    NOT_TAKEN,
    UNKNOWN
}
class MedicineTake(val medicine: Medicine, val dateOfTake: Date, val takeStatus: TakeStatus)
