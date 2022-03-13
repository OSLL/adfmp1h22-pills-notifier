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
    DAILY("Daily"),
    ONCE_IN_TWO_DAYS("Once in 2 days"),
    ONCE_A_WEEK("Once a week")
}

class Medicine {
    var medicine_id: String? = null
    var medicine_name: String? = null
    var portion: Portion? = null
    var time: String? = null
    var regularity: Regularity? = null
    var start_date: String? = null
    var end_date: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(medicine_id: String,
                medicine_name: String,
                portion: Portion,
                time: String,
                regularity: Regularity,
                start_date: String,
                end_date: String) {
        this.medicine_id = medicine_id
        this.medicine_name = medicine_name
        this.portion = portion
        this.regularity = regularity
        this.time = time
        this.start_date = start_date
        this.end_date = end_date
    }

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(medicine_id: String,
                medicine_name: String,
                portion: Portion,
                time: LocalTime,
                regularity: Regularity,
                start_date: LocalDate,
                end_date: LocalDate) :
        this(medicine_id=medicine_id, medicine_name=medicine_name, portion=portion,
            time=time.format(timeFormat), regularity=regularity,
            start_date=start_date.format(dateFormat), end_date=end_date.format(dateFormat))
}