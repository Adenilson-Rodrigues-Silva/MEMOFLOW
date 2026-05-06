package com.arsdevstudio.memoflow.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WeeklyInsightWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = NotificationPrefs(applicationContext)
        val helper = NotificationHelper(applicationContext)
        
        runBlocking {
            val settings = prefs.notificationSettings.first()
            if (settings.allEnabled && settings.insightEnabled) {
                val phrase = NotificationPhrases.getRandomPhrase(NotificationPhrases.weeklyInsight)
                helper.showNotification(
                    channelId = NotificationHelper.CHANNEL_INSIGHT,
                    title = "Insight Semanal",
                    message = phrase,
                    soundEnabled = settings.soundEnabled,
                    vibrationEnabled = settings.vibrationEnabled
                )
            }
        }
        return Result.success()
    }
}

