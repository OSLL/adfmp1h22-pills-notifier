package com.example.pillnotifier.model

import java.util.Date

typealias Portion = String

enum class Regularity {
    DAILY
}

class Medicine(
    val name: String,
    val portion: Portion,
    val regularity: Regularity,
    firstTakeDate: Date
)