package com.rabbitsluckandfortuneppamobs.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rabbitsluckandfortuneppamobs.models.PlayerProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Single DataStore instance for the whole app (spec §6: offline local storage).
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fortune_rabbit_progress")

/**
 * Persists [PlayerProgress] as one JSON blob in DataStore Preferences.
 * All game data is stored locally so the app works fully offline.
 */
class ProgressStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val progressKey = stringPreferencesKey("player_progress_json")

    val progressFlow: Flow<PlayerProgress> = context.dataStore.data.map { prefs ->
        prefs[progressKey]?.let { raw ->
            runCatching { json.decodeFromString<PlayerProgress>(raw) }.getOrNull()
        } ?: PlayerProgress()
    }

    suspend fun save(progress: PlayerProgress) {
        val encoded = json.encodeToString(progress)
        context.dataStore.edit { prefs -> prefs[progressKey] = encoded }
    }

    suspend fun reset() {
        save(PlayerProgress())
    }
}
