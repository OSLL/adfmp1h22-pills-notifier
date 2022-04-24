package com.example.pillnotifier

import android.content.Context
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.Medicine
import java.io.*

const val medicineListFileName = "medicineList"
fun cachingMedicineList(context: Context, medicineList: List<Medicine>) {
    ObjectOutputStream(
        context.openFileOutput(
            medicineListFileName,
            Context.MODE_PRIVATE
        )
    ).writeObject(
        medicineList
    )
}

fun cachingNewMedicine(context: Context, medicine: Medicine) {
    val oldList = getCachedMedicineList(context)
    ObjectOutputStream(
        context.openFileOutput(
            medicineListFileName,
            Context.MODE_APPEND
        )
    ).writeObject(
        oldList.toMutableList().apply { add(medicine) }.toList()
    )
}

fun getCachedMedicineList(context: Context): List<Medicine> {
    return ObjectInputStream(context.openFileInput(medicineListFileName)).readObject() as List<Medicine>
}

const val curUsedIdCacheFileName = "userIdCacheFile"
fun cachingCurrentUserId(context: Context) {
    ObjectOutputStream(
        context.openFileOutput(
            curUsedIdCacheFileName,
            Context.MODE_PRIVATE
        )
    ).writeObject(DataHolder.getData("userId"))
}

fun getCachedUserId(context: Context): String {
    return ObjectInputStream(context.openFileInput(curUsedIdCacheFileName)).readObject() as String
}