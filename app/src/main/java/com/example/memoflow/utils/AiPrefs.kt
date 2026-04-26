package com.example.memoflow.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "ai_prefs")

class AiPrefs(private val context: Context) {
    private val gson = Gson()

    companion object {
        private val LAST_RESET_DATE = stringPreferencesKey("last_reset_date")
        private val DAILY_COUNTS = stringPreferencesKey("daily_counts")
        private val NEXT_AVAILABLE_TIMES = stringPreferencesKey("next_available_times")
    }

    suspend fun getDailyCounts(): Map<String, Int> {
        val lastReset = context.dataStore.data.map { it[LAST_RESET_DATE] }.first()
        val today = LocalDate.now().toString()

        if (lastReset != today) {
            resetDailyCounts(today)
            return emptyMap()
        }

        val json = context.dataStore.data.map { it[DAILY_COUNTS] }.first() ?: return emptyMap()
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(json, type)
    }

    suspend fun saveDailyCounts(counts: Map<String, Int>) {
        val json = gson.toJson(counts)
        context.dataStore.edit { it[DAILY_COUNTS] = json }
        context.dataStore.edit { it[LAST_RESET_DATE] = LocalDate.now().toString() }
    }

    suspend fun getNextAvailableTimes(): Map<String, Long> {
        val json = context.dataStore.data.map { it[NEXT_AVAILABLE_TIMES] }.first() ?: return emptyMap()
        val type = object : TypeToken<Map<String, Long>>() {}.type
        return gson.fromJson(json, type)
    }

    suspend fun saveNextAvailableTimes(times: Map<String, Long>) {
        val json = gson.toJson(times)
        context.dataStore.edit { it[NEXT_AVAILABLE_TIMES] = json }
    }

    private suspend fun resetDailyCounts(today: String) {
        context.edit {
            it[DAILY_COUNTS] = "{}"
            it[LAST_RESET_DATE] = today
        }
    }
}

private suspend fun Context.edit(transform: suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
    dataStore.edit(transform)
}
