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
import kotlin.random.Random

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_DAILY = "daily"
        const val CHANNEL_GRATITUDE = "gratitude"
        const val CHANNEL_CAPSULE = "capsule"
        const val CHANNEL_INSIGHT = "insight"
        const val CHANNEL_EVENTS = "events"

        // v12 para resetar qualquer política de silenciamento do sistema
        private const val SUFFIX_S1V1 = "_s1v1_v12"
        private const val SUFFIX_S1V0 = "_s1v0_v12"
        private const val SUFFIX_S0V1 = "_s0v1_v12"
        private const val SUFFIX_S0V0 = "_s0v0_v12"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val categories = listOf(
                CHANNEL_DAILY to "Momento Flow",
                CHANNEL_GRATITUDE to "Pote de Gratidão",
                CHANNEL_CAPSULE to "Descongelamento",
                CHANNEL_INSIGHT to "Insight Semanal",
                CHANNEL_EVENTS to "Eventos Especiais"
            )

            categories.forEach { (id, name) ->
                createChannelVariant(id + SUFFIX_S1V1, name, hasSound = true, hasVib = true)
                createChannelVariant(id + SUFFIX_S1V0, "$name (Sem Vibração)", hasSound = true, hasVib = false)
                createChannelVariant(id + SUFFIX_S0V1, "$name (Silencioso)", hasSound = false, hasVib = true)
                createChannelVariant(id + SUFFIX_S0V0, "$name (Mudo)", hasSound = false, hasVib = false)
            }
        }
    }

    private fun createChannelVariant(id: String, name: String, hasSound: Boolean, hasVib: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance).apply {
                if (hasSound) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(getSoundUriForCategory(id), audioAttributes)
                } else {
                    setSound(null, null)
                }

                enableVibration(hasVib)
                vibrationPattern = if (hasVib) longArrayOf(0, 400, 200, 400) else null
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        channelId: String, 
        title: String, 
        message: String, 
        soundEnabled: Boolean = true, 
        vibrationEnabled: Boolean = true
    ) {
        val suffix = when {
            soundEnabled && vibrationEnabled -> SUFFIX_S1V1
            soundEnabled && !vibrationEnabled -> SUFFIX_S1V0
            !soundEnabled && vibrationEnabled -> SUFFIX_S0V1
            else -> SUFFIX_S0V0
        }
        val targetChannelId = channelId + suffix

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, targetChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false) // CRUCIAL: Força alertar toda vez

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 400, 200, 400))
        }

        // Usa ID aleatório para evitar que o sistema agrupe/silencie como "update"
        val notificationId = Random.nextInt(1000, 999999)
        notificationManager.notify(notificationId, builder.build())
    }

    private fun getSoundUriForCategory(channelFullId: String): Uri {
        val res = when {
            channelFullId.contains(CHANNEL_DAILY) -> R.raw.sound_pulse
            channelFullId.contains(CHANNEL_EVENTS) -> R.raw.sound_echo
            else -> R.raw.sound_crystal
        }
        return Uri.parse("android.resource://${context.packageName}/$res")
    }
}
