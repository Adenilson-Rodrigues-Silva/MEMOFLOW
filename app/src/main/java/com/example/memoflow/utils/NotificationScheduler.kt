package com.example.memoflow.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val TAG = "NotificationScheduler"

    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val workName = "daily_reminder_work"
        val delay = calculateDelay(hour, minute)
        
        Log.d(TAG, "Agendando lembrete diário para $hour:$minute. Delay calculado: ${delay/1000} segundos.")

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName, 
            ExistingPeriodicWorkPolicy.UPDATE, 
            dailyWorkRequest
        )
    }

    fun scheduleGratitudeReminder() {
        val delay = calculateDelay(15, 0)
        Log.d(TAG, "Agendando gratidão às 15:00. Delay: ${delay/1000}s")
        
        val request = PeriodicWorkRequestBuilder<GratitudeWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("gratitude")
            .build()
        workManager.enqueueUniquePeriodicWork("gratitude_work", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun scheduleTimeCapsuleReminder() {
        val delay = calculateDelay(9, 0)
        Log.d(TAG, "Agendando cápsula às 09:00. Delay: ${delay/1000}s")
        
        val request = PeriodicWorkRequestBuilder<TimeCapsuleWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("capsule")
            .build()
        workManager.enqueueUniquePeriodicWork("capsule_work", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun scheduleWeeklyInsight() {
        val delay = calculateWeeklyDelay(Calendar.SUNDAY, 18, 0)
        Log.d(TAG, "Agendando insight domingo às 18:00. Delay: ${delay/1000}s")
        
        val request = PeriodicWorkRequestBuilder<WeeklyInsightWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("insight")
            .build()
        workManager.enqueueUniquePeriodicWork("insight_work", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun scheduleLockedNotesReminder() {
        val delay = calculateWeeklyDelay(Calendar.SATURDAY, 11, 0)
        Log.d(TAG, "Agendando ecos sábado às 11:00. Delay: ${delay/1000}s")
        
        val request = PeriodicWorkRequestBuilder<LockedNotesWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("locked_notes")
            .build()
        workManager.enqueueUniquePeriodicWork("locked_work", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun scheduleNewYearCapsule() {
        val delay = calculateYearlyDelay(Calendar.JANUARY, 1, 9, 0)
        Log.d(TAG, "Agendando ano novo. Delay: ${delay/1000}s")
        
        val request = OneTimeWorkRequestBuilder<NewYearWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("new_year")
            .build()
        workManager.enqueueUniqueWork("new_year_work", ExistingWorkPolicy.KEEP, request)
    }

    private fun calculateDelay(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Margem de erro: se faltar menos de 30 segundos para o horário, joga para amanhã
            // Isso evita que o agendamento tente rodar um tempo que já passou por milissegundos
            if (timeInMillis <= System.currentTimeMillis() + 30000) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis - System.currentTimeMillis()
    }

    private fun calculateWeeklyDelay(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis() + 30000) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis - System.currentTimeMillis()
    }

    private fun calculateYearlyDelay(month: Int, dayOfMonth: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis() + 30000) {
                add(Calendar.YEAR, 1)
            }
        }
        return calendar.timeInMillis - System.currentTimeMillis()
    }

    fun cancelWork(tag: String) {
        Log.d(TAG, "Cancelando trabalho com tag: $tag")
        workManager.cancelAllWorkByTag(tag)
    }

    fun cancelAll() {
        Log.d(TAG, "Cancelando TODAS as notificações agendadas")
        workManager.cancelAllWork()
    }
}
