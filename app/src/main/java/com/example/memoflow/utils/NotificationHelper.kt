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
        const val CHANNEL_DAILY = "daily_reminder_channel"
        const val CHANNEL_CAPSULE = "time_capsule_channel"
        const val CHANNEL_GRATITUDE = "gratitude_channel"
        const val CHANNEL_INSIGHT = "weekly_insight_channel"
        const val CHANNEL_EVENTS = "special_events_channel"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundPulse = Uri.parse("android.resource://${context.packageName}/${R.raw.sound_pulse}")
            val soundCrystal = Uri.parse("android.resource://${context.packageName}/${R.raw.sound_crystal}")
            val soundEcho = Uri.parse("android.resource://${context.packageName}/${R.raw.sound_echo}")

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channels = listOf(
                NotificationChannel(CHANNEL_DAILY, "Momento Flow", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Lembretes diários para escrever"
                    setSound(soundPulse, audioAttributes)
                },
                NotificationChannel(CHANNEL_CAPSULE, "Descongelamento", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Avisos de cápsulas do tempo abertas"
                    setSound(soundCrystal, audioAttributes)
                },
                NotificationChannel(CHANNEL_GRATITUDE, "Pote de Gratidão", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Incentivos para o mural de gratidão"
                    setSound(soundCrystal, audioAttributes)
                },
                NotificationChannel(CHANNEL_INSIGHT, "Insight Semanal", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Resumo semanal de humores"
                    setSound(soundCrystal, audioAttributes)
                },
                NotificationChannel(CHANNEL_EVENTS, "Eventos Especiais", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Cápsula da Virada e eventos anuais"
                    setSound(soundEcho, audioAttributes)
                }
            )

            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    fun showNotification(channelId: String, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Carrega o seu ic_app como uma imagem grande (Large Icon)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_app)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Ícone genérico para evitar o quadrado preto
            .setLargeIcon(largeIcon) // Seu ícone bonito aparece ao lado do texto
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
