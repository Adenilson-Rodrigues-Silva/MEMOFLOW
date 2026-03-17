package com.example.memoflow.utils

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(context)
        
        // Nome único para evitar múltiplas instâncias da mesma tarefa
        val workName = "daily_reminder_work"

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Se o horário já passou hoje, agenda para amanhã
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        // Usamos PeriodicWork para garantir que rode todo dia
        // O intervalo mínimo permitido pelo Android é 15 minutos, mas queremos 24h
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE, // Atualiza se já existir, mantendo o novo horário
            dailyWorkRequest
        )
    }

    fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWork()
    }
}
