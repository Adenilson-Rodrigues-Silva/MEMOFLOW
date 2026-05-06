package com.arsdevstudio.memoflow.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DailyReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = NotificationPrefs(applicationContext)
        val helper = NotificationHelper(applicationContext)
        
        runBlocking {
            val settings = prefs.notificationSettings.first()
            if (settings.allEnabled && settings.dailyEnabled) {
                val phrase = NotificationPhrases.getRandomPhrase(NotificationPhrases.dailyReminder)
                helper.showNotification(
                    channelId = NotificationHelper.CHANNEL_DAILY,
                    title = "Momento Flow",
                    message = phrase,
                    soundEnabled = settings.soundEnabled,
                    vibrationEnabled = settings.vibrationEnabled
                )
            }
        }
        return Result.success()
    }
}

