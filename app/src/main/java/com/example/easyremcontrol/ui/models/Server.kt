package com.example.easyremcontrol.ui.models
import kotlinx.serialization.Serializable

@Serializable
// Модель данных для сервера
data class Server(
    val name: String,      // Имя сервера
    val ip: String,        // IP-адрес сервера
    val username: String,  // Логин для подключения
    val password: String   // Пароль для подключения
)
