package com.example.easyremcontrol.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyremcontrol.ui.models.Server
import com.example.easyremcontrol.utils.executeSSHCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.ChannelExec
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

// Функция для получения списка сервисов с сервера
suspend fun getServicesList(server: Server): List<Pair<String, Boolean>> {
    return try {
        val result = executeSSHCommand(server, "systemctl list-units --type=service --all --no-pager")
        result.split("\n")
            .filter { it.contains(".service") }
            .map { line ->
                val isActive = line.contains("running")
                val serviceName = line.substringBefore(".service") + ".service"
                serviceName to isActive
            }
    } catch (e: Exception) {
        emptyList()  // Возвращаем пустой список в случае ошибки
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    selectedServer: Server?,
    onBackPress: () -> Unit // Добавляем обработчик для кнопки "Назад"
) {
    var servicesList by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
    var isLoading by remember { mutableStateOf(false) }  // Индикатор загрузки
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }  // Хост для Snackbar
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление сервисами") },
                navigationIcon = {
                    IconButton(onClick = { onBackPress() }) { // Добавляем кнопку "Назад"
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        snackbarHost = {  // Для Snackbar уведомлений
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (selectedServer == null) {
                    Text("Выберите сервер для управления сервисами")
                } else {
                    // Кнопка для получения списка сервисов
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    isLoading = true  // Показываем индикатор загрузки
                                    servicesList = getServicesList(selectedServer)
                                    if (servicesList.isEmpty()) {
                                        snackbarMessage = "Список сервисов пуст или произошла ошибка"
                                        snackbarHostState.showSnackbar(snackbarMessage)
                                    }
                                } finally {
                                    isLoading = false  // Прекращаем показ индикатора
                                }
                            }
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text("Проверить статус сервисов")
                    }

                    // Показ индикатора загрузки во время получения списка сервисов
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }

                    // Отображение списка сервисов
                    LazyColumn {
                        items(servicesList) { (service, isActive) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = service,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.Green else Color.Red,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    // Кнопка для запуска сервиса (доступна для остановленных сервисов)
                                    if (!isActive) {
                                        IconButton(onClick = {
                                            coroutineScope.launch {
                                                try {
                                                    isLoading = true
                                                    executeSSHCommand(selectedServer, "sudo systemctl start $service")
                                                    snackbarHostState.showSnackbar("$service запущен")
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Filled.PlayArrow,
                                                contentDescription = "Start Service",
                                                tint = Color.Green
                                            )
                                        }
                                    }

                                    // Кнопка для остановки сервиса (доступна для активных сервисов)
                                    if (isActive) {
                                        IconButton(onClick = {
                                            coroutineScope.launch {
                                                try {
                                                    isLoading = true
                                                    executeSSHCommand(selectedServer, "sudo systemctl stop $service")
                                                    snackbarHostState.showSnackbar("$service остановлен")
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Filled.Stop,
                                                contentDescription = "Stop Service",
                                                tint = Color.Red
                                            )
                                        }
                                    }

                                    // Кнопка для перезапуска сервиса
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            try {
                                                isLoading = true
                                                executeSSHCommand(selectedServer, "sudo systemctl restart $service")
                                                snackbarHostState.showSnackbar("$service перезапущен")
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Autorenew,
                                            contentDescription = "Reboot Service",
                                            tint = Color.Blue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
