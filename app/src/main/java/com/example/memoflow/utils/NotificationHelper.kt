package com.example.memoflow.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.memoflow.MainActivity
import com.example.memoflow.R

class NotificationHelper(private val context: Context) {

    companion object {
        // IDs de referência usados pelo resto do App
        const val CHANNEL_DAILY = "daily_ref"
        const val CHANNEL_GRATITUDE = "gratitude_ref"
        const val CHANNEL_CAPSULE = "capsule_ref"
        const val CHANNEL_INSIGHT = "insight_ref"
        const val CHANNEL_EVENTS = "events_ref"

        // Canais REAIS v7 (Som)
        private const val CH_DAILY_SOUND = "daily_sound_v7"
        private const val CH_GRATITUDE_SOUND = "gratitude_sound_v7"
        private const val CH_CAPSULE_SOUND = "capsule_sound_v7"
        private const val CH_INSIGHT_SOUND = "insight_sound_v7"
        private const val CH_EVENTS_SOUND = "events_sound_v7"

        // Canais REAIS v7 (Silencioso/Vibração)
        private const val CH_DAILY_SILENT = "daily_silent_v7"
        private const val CH_GRATITUDE_SILENT = "gratitude_silent_v7"
        private const val CH_CAPSULE_SILENT = "capsule_silent_v7"
        private const val CH_INSIGHT_SILENT = "insight_silent_v7"
        private const val CH_EVENTS_SILENT = "events_silent_v7"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val config = listOf(
                Triple(CH_DAILY_SOUND, CH_DAILY_SILENT, "Momento Flow"),
                Triple(CH_GRATITUDE_SOUND, CH_GRATITUDE_SILENT, "Pote de Gratidão"),
                Triple(CH_CAPSULE_SOUND, CH_CAPSULE_SILENT, "Descongelamento"),
                Triple(CH_INSIGHT_SOUND, CH_INSIGHT_SILENT, "Insight Semanal"),
                Triple(CH_EVENTS_SOUND, CH_EVENTS_SILENT, "Eventos Especiais")
            )

            config.forEach { (soundId, silentId, name) ->
                // Canal Som
                val soundUri = getSoundUriForChannel(soundId)
                notificationManager.createNotificationChannel(
                    NotificationChannel(soundId, "$name (Som)", NotificationManager.IMPORTANCE_HIGH).apply {
                        setSound(soundUri, audioAttributes)
                        enableVibration(true)
                    }
                )
                // Canal Silencioso
                notificationManager.createNotificationChannel(
                    NotificationChannel(silentId, "$name (Vibração)", NotificationManager.IMPORTANCE_HIGH).apply {
                        setSound(null, null)
                        enableVibration(true)
                    }
                )
            }
        }
    }

    fun showNotification(
        channelId: String, 
        title: String, 
        message: String, 
        soundEnabled: Boolean = true, 
        vibrationEnabled: Boolean = true
    ) {
        // Mapeia o canal de referência para o canal REAL baseado na preferência de som
        val targetChannelId = when (channelId) {
            CHANNEL_DAILY -> if (soundEnabled) CH_DAILY_SOUND else CH_DAILY_SILENT
            CHANNEL_GRATITUDE -> if (soundEnabled) CH_GRATITUDE_SOUND else CH_GRATITUDE_SILENT
            CHANNEL_CAPSULE -> if (soundEnabled) CH_CAPSULE_SOUND else CH_CAPSULE_SILENT
            CHANNEL_INSIGHT -> if (soundEnabled) CH_INSIGHT_SOUND else CH_INSIGHT_SILENT
            CHANNEL_EVENTS -> if (soundEnabled) CH_EVENTS_SOUND else CH_EVENTS_SILENT
            else -> if (soundEnabled) CH_DAILY_SOUND else CH_DAILY_SILENT
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, targetChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_app))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 400, 200, 400))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun getSoundUriForChannel(id: String): Uri {
        val res = when {
            id.contains("daily") -> R.raw.sound_pulse
            id.contains("events") -> R.raw.sound_echo
            else -> R.raw.sound_crystal
        }
        return Uri.parse("android.resource://${context.packageName}/$res")
    }
}
