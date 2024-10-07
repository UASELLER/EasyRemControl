package com.example.easyremcontrol.ui.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.easyremcontrol.ui.screens.LogEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore("logs_prefs")

object LogDataStore {
    private val LOGS_KEY = stringPreferencesKey("logs_key")

    // Метод для загрузки логов
    suspend fun loadLogs(context: Context): List<LogEntry> {
        val preferences = context.dataStore.data.map { prefs ->
            prefs[LOGS_KEY] ?: ""
        }.first()

        return if (preferences.isNotEmpty()) {
            Json.decodeFromString(preferences)
        } else {
            emptyList()
        }
    }

    // Метод для сохранения логов
    suspend fun saveLogs(context: Context, logsList: List<LogEntry>) {
        val logsJson = Json.encodeToString(logsList)
        context.dataStore.edit { prefs ->
            prefs[LOGS_KEY] = logsJson
        }
    }
}
