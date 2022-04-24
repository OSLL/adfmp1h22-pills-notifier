package com.example.pillnotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.pillnotifier.model.DataHolder
import com.example.pillnotifier.model.Medicine
import com.example.pillnotifier.model.getMedicineListFromServer
import kotlinx.coroutines.DelicateCoroutinesApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@OptIn(DelicateCoroutinesApi::class)
class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    companion object {
        private var notificationIdCounter = 100
        private const val CHANNEL_ID = "Take medicine reminding"
        const val NOTIFY_WORK_TAG = "NOTIFY_WORK_TAG"
        @RequiresApi(Build.VERSION_CODES.O)
        private var prevNotifyTime: LocalDateTime? = LocalDateTime.now()
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_ID
            val descriptionText = ""
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkMedicineHaveToBeTakenInPeriod(
        startTime: LocalDateTime, endTime: LocalDateTime, medicine: Medicine
    ): Boolean {
        var takeTime =
            LocalDateTime.of(LocalDate.parse(medicine.start_date), LocalTime.parse(medicine.time))
        val medicineLastTakeTime =
            LocalDateTime.of(LocalDate.parse(medicine.end_date), LocalTime.parse(medicine.time))
        while (takeTime < endTime && takeTime <= medicineLastTakeTime) {
            if (startTime <= takeTime) {
                return true
            }
            takeTime += medicine.regularity!!.timeDelta
        }
        return false
    }

    private suspend fun updateCash() {
        DataHolder.setData("userId", getCachedUserId(context))
        val res = getMedicineListFromServer(context)
        if (res.success != null) {
            cachingMedicineList(context, res.success)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        // update cache
        updateCash()
        val nowTime = LocalDateTime.now()
        // get list of medicines, that have to be taken in cur period
        val medicinesForNotify = getCachedMedicineList(context)
            // commented for test
            .filter {
                checkMedicineHaveToBeTakenInPeriod(
                    prevNotifyTime!!,
                    nowTime,
                    it
                )
            }
//            // TODO filter by their status if it possible.
//            // push notifications
        for (med in medicinesForNotify) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.medicine_icon)
                .setContentTitle("Take ${med.medicine_name}")
                .setContentText("${med.portion} at ${med.time}")
                .setPriority(NotificationCompat.PRIORITY_MAX)

            with(NotificationManagerCompat.from(context)) {
                notify(notificationIdCounter, builder.build())
            }
            notificationIdCounter++
//                Toast.makeText(context, "$notificationIdCounter", Toast.LENGTH_SHORT).show()
        }
        prevNotifyTime = nowTime
        val myWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInitialDelay(20, TimeUnit.SECONDS)
            .addTag(NOTIFY_WORK_TAG).build()
        WorkManager.getInstance(context).enqueue(myWorkRequest)
        return Result.success()
    }
}