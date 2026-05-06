package com.arsdevstudio.memoflow.utils

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "map_prefs")

class MapPrefs(private val context: Context) {
    companion object {
        private val LAST_LAT = doublePreferencesKey("last_lat")
        private val LAST_LNG = doublePreferencesKey("last_lon")
        private val LAST_ZOOM = floatPreferencesKey("last_zoom")
        
        // Default: Portugal (Lisboa)
        const val DEFAULT_LAT = 38.7223
        const val DEFAULT_LNG = -9.1393
        const val DEFAULT_ZOOM = 10f
    }

    val lastLocation: Flow<Triple<Double, Double, Float>> = context.dataStore.data.map { prefs ->
        Triple(
            prefs[LAST_LAT] ?: DEFAULT_LAT,
            prefs[LAST_LNG] ?: DEFAULT_LNG,
            prefs[LAST_ZOOM] ?: DEFAULT_ZOOM
        )
    }

    suspend fun saveLocation(lat: Double, lng: Double, zoom: Float) {
        context.dataStore.edit { prefs ->
            prefs[LAST_LAT] = lat
            prefs[LAST_LNG] = lng
            prefs[LAST_ZOOM] = zoom
        }
    }
}

