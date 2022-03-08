package com.example.pillnotifier.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

typealias Portion = String
@RequiresApi(Build.VERSION_CODES.O)
val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
@RequiresApi(Build.VERSION_CODES.O)
val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

enum class Regularity(val stringInterpretation: String) {
    DAILY("DAILY"),
    ONCE_IN_TWO_DAYS("ONCE_IN_TWO_DAYS"),
    ONCE_A_WEEK("ONCE_A_WEEK")
}

class Medicine {
    var medicine_name: String? = null
    var portion: Portion? = null
    var time: String? = null
    var regularity: Regularity? = null
    var date: String? = null
    var instructions: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(medicine_name: String,
                portion: Portion,
                time: String,
                regularity: Regularity,
                date: String,
                instructions: String) {
        this.medicine_name = medicine_name
        this.portion = portion
        this.regularity = regularity
        this.instructions = instructions
        this.time = time
        this.date = date
    }

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(medicine_name: String,
                portion: Portion,
                time: LocalTime,
                regularity: Regularity,
                date: LocalDate,
                instructions: String) :
        this(medicine_name=medicine_name, portion=portion, time=time.format(timeFormat),
            regularity=regularity, date=date.format(dateFormat), instructions=instructions)
}