package com.arsdevstudio.memoflow.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NewYearWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = NotificationPrefs(applicationContext)
        val helper = NotificationHelper(applicationContext)
        
        runBlocking {
            val settings = prefs.notificationSettings.first()
            if (settings.allEnabled && settings.newYearEnabled) {
                val phrase = NotificationPhrases.getRandomPhrase(NotificationPhrases.newYearCapsule)
                helper.showNotification(
                    channelId = NotificationHelper.CHANNEL_EVENTS,
                    title = "Cápsula da Virada",
                    message = phrase
                )
            }
        }
        return Result.success()
    }
}

