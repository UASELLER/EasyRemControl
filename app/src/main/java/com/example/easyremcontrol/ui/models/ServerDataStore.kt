package com.example.easyremcontrol.ui.datastore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.easyremcontrol.ui.models.Server
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ServerDataStore {

    // Инициализация зашифрованного SharedPreferences
    private fun getEncryptedPreferences(context: Context): EncryptedSharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            "encrypted_server_data",  // Имя файла
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    // Получение списка серверов из зашифрованного SharedPreferences
    fun getServers(context: Context): List<Server> {
        val encryptedPreferences = getEncryptedPreferences(context)
        val serversJson = encryptedPreferences.getString("servers", "") ?: ""

        return if (serversJson.isNotEmpty()) {
            Json.decodeFromString(serversJson)
        } else {
            emptyList()
        }
    }

    // Сохранение списка серверов в зашифрованное SharedPreferences
    fun saveServers(context: Context, servers: List<Server>) {
        val encryptedPreferences = getEncryptedPreferences(context)
        val serversJson = Json.encodeToString(servers)

        encryptedPreferences.edit().putString("servers", serversJson).apply()
    }

    // Очистка всех серверов
    fun clearServers(context: Context) {
        val encryptedPreferences = getEncryptedPreferences(context)
        encryptedPreferences.edit().clear().apply()
    }
}
