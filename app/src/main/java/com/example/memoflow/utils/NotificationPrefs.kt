package com.example.memoflow.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "notification_prefs")

class NotificationPrefs(private val context: Context) {
    companion object {
        val ALL_ENABLED = booleanPreferencesKey("all_enabled")
        val DAILY_ENABLED = booleanPreferencesKey("daily_enabled")
        val DAILY_HOUR = intPreferencesKey("daily_hour")
        val DAILY_MINUTE = intPreferencesKey("daily_minute")
        val CAPSULE_ENABLED = booleanPreferencesKey("capsule_enabled")
        val GRATITUDE_ENABLED = booleanPreferencesKey("gratitude_enabled")
        val ECHO_ENABLED = booleanPreferencesKey("echo_enabled")
        val INSIGHT_ENABLED = booleanPreferencesKey("insight_enabled")
        val NEW_YEAR_ENABLED = booleanPreferencesKey("new_year_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }

    val notificationSettings: Flow<NotificationSettings> = context.dataStore.data.map { prefs ->
        NotificationSettings(
            allEnabled = prefs[ALL_ENABLED] ?: true,
            dailyEnabled = prefs[DAILY_ENABLED] ?: true,
            dailyHour = prefs[DAILY_HOUR] ?: 20,
            dailyMinute = prefs[DAILY_MINUTE] ?: 0,
            capsuleEnabled = prefs[CAPSULE_ENABLED] ?: true,
            gratitudeEnabled = prefs[GRATITUDE_ENABLED] ?: true,
            echoEnabled = prefs[ECHO_ENABLED] ?: false,
            insightEnabled = prefs[INSIGHT_ENABLED] ?: true,
            newYearEnabled = prefs[NEW_YEAR_ENABLED] ?: true,
            soundEnabled = prefs[SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[VIBRATION_ENABLED] ?: true
        )
    }

    suspend fun updateSettings(update: (MutablePreferences) -> Unit) {
        context.dataStore.edit { update(it) }
    }
}

data class NotificationSettings(
    val allEnabled: Boolean,
    val dailyEnabled: Boolean,
    val dailyHour: Int,
    val dailyMinute: Int,
    val capsuleEnabled: Boolean,
    val gratitudeEnabled: Boolean,
    val echoEnabled: Boolean,
    val insightEnabled: Boolean,
    val newYearEnabled: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean
)
