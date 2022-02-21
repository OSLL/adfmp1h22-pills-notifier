package com.example.pillnotifier.model

import java.time.LocalDate
import java.time.LocalTime

typealias Portion = String

enum class Regularity(val stringInterpretation: String) {
    DAILY("Daily")
}

class Medicine(
    val name: String,
    val portion: Portion,
    val takeTime: LocalTime,
    val regularity: Regularity,
    val startTakeDate: LocalDate,
    val instruction: String = ""
)