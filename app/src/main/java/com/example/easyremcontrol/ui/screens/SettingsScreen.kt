package com.example.easyremcontrol.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.easyremcontrol.ui.datastore.ServerDataStore
import com.example.easyremcontrol.ui.models.Server
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    selectedTheme: String,  // Передаем текущую выбранную тему
    onThemeChange: (String) -> Unit,  // Функция для изменения темы
    onBackPress: () -> Unit,
    context: Context
) {
    val themes = listOf("Светлая", "Тёмная", "Матрица")  // Возможные темы
    var servers by remember { mutableStateOf(ServerDataStore.getServers(context)) }
    val serverName = remember { mutableStateOf("") }
    val serverIP = remember { mutableStateOf("") }
    val serverUsername = remember { mutableStateOf("") }
    val serverPassword = remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    var errorMessage by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }  // Для раскрывающегося меню тем

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { onBackPress() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Центрирование контента
            ) {
                // Выбор темы через DropdownMenu
                Text("Выбор темы", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(8.dp))

                // Box для корректного расположения кнопки и выпадающего списка
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(selectedTheme) // Отображаем текущую тему
                    }

                    // DropdownMenu привязан к кнопке, появляется сразу под ней
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        themes.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme) },
                                onClick = {
                                    onThemeChange(theme)  // Обновляем тему
                                    expanded = false      // Закрываем меню после выбора
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Поля для добавления сервера
                OutlinedTextField(
                    value = serverName.value,
                    onValueChange = { serverName.value = it },
                    label = { Text("Имя сервера") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = serverIP.value,
                    onValueChange = { serverIP.value = it },
                    label = { Text("IP-адрес") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = serverUsername.value,
                    onValueChange = { serverUsername.value = it },
                    label = { Text("Логин") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = serverPassword.value,
                    onValueChange = { serverPassword.value = it },
                    label = { Text("Пароль") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Сообщение об ошибке (если есть)
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Заголовок "Подключенные сервера"
                Text(
                    text = "Подключенные сервера",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                // Список серверов
                LazyColumn {
                    items(servers) { server ->
                        ServerItem(server = server, onDelete = {
                            coroutineScope.launch {
                                val updatedServers = servers.filter { it != server }
                                ServerDataStore.saveServers(context, updatedServers)
                                servers = updatedServers
                            }
                        })
                    }
                }

                // Кнопка для добавления сервера
                Button(onClick = {
                    coroutineScope.launch {
                        try {
                            // Проверка на наличие сервера с таким же именем или IP
                            val existingServer = servers.find {
                                it.name == serverName.value || it.ip == serverIP.value
                            }
                            if (existingServer != null) {
                                errorMessage = "Сервер с таким именем или IP уже существует"
                            } else {
                                val newServer = Server(
                                    name = serverName.value,
                                    ip = serverIP.value,
                                    username = serverUsername.value,
                                    password = serverPassword.value
                                )
                                val updatedServers = servers + newServer
                                ServerDataStore.saveServers(context, updatedServers)
                                servers = updatedServers

                                // Очистка полей после добавления сервера
                                serverName.value = ""
                                serverIP.value = ""
                                serverUsername.value = ""
                                serverPassword.value = ""
                                errorMessage = ""  // Сбрасываем сообщение об ошибке
                            }
                        } catch (e: Exception) {
                            errorMessage = "Ошибка при добавлении сервера: ${e.message}"
                        }
                    }
                }) {
                    Text("Добавить сервер")
                }
            }
        }
    )
}

@Composable
fun ServerItem(server: Server, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Имя: ${server.name}")
            Text("IP: ${server.ip}")
            Text("Логин: ${server.username}")
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete"
            )
        }
    }
}
