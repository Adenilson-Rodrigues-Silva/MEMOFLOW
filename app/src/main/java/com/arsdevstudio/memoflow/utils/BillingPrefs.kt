package com.arsdevstudio.memoflow.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "billing_prefs")

class BillingPrefs(private val context: Context) {
    companion object {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val LAST_DONATION_SHOWN = longPreferencesKey("last_donation_shown")
        val LAST_SUPPORT_POPUP_DATE = androidx.datastore.preferences.core.stringPreferencesKey("last_support_popup_date")
    }

    val lastSupportPopupDate: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LAST_SUPPORT_POPUP_DATE]
    }

    suspend fun setLastSupportPopupDate(date: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SUPPORT_POPUP_DATE] = date
        }
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_PREMIUM] ?: false
    }

    val lastDonationShown: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_DONATION_SHOWN] ?: 0L
    }

    suspend fun setPremium(premium: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_PREMIUM] = premium
        }
    }

    suspend fun updateLastDonationShown(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_DONATION_SHOWN] = timestamp
        }
    }
}

