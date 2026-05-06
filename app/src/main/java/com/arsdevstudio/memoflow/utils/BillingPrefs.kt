package com.arsdevstudio.memoflow.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "billing_prefs")

class BillingPrefs(private val context: Context) {
    companion object {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_PREMIUM] ?: false
    }

    suspend fun setPremium(premium: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_PREMIUM] = premium
        }
    }
}

