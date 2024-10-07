package com.example.easyremcontrol.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyremcontrol.ui.datastore.LogDataStore
import com.example.easyremcontrol.ui.models.Server
import com.example.easyremcontrol.utils.executeSSHCommand
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    selectedServer: Server?,
    onBackPress: () -> Unit
) {
    val context = LocalContext.current
    var logsList by remember { mutableStateOf(mutableListOf<LogEntry>()) }
    var isDialogOpen by remember { mutableStateOf(false) }
    var logToView by remember { mutableStateOf<LogEntry?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Загрузка логов при инициализации экрана
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                logsList = LogDataStore.loadLogs(context).toMutableList()
            } catch (e: Exception) {
                e.printStackTrace()
                snackbarHostState.showSnackbar("Ошибка загрузки логов")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Логи") },
                navigationIcon = {
                    IconButton(onClick = { onBackPress() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isDialogOpen = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Добавить лог")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(logsList) { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = log.name,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Row {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        try {
                                            logsList.remove(log)
                                            LogDataStore.saveLogs(context, logsList)
                                            snackbarHostState.showSnackbar("${log.name} удален")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            snackbarHostState.showSnackbar("Ошибка при удалении лога")
                                        }
                                    }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Удалить лог", tint = Color.Red)
                                }

                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        try {
                                            clearLog(selectedServer, log.path)
                                            snackbarHostState.showSnackbar("${log.name} очищен")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            snackbarHostState.showSnackbar("Ошибка при очистке лога")
                                        }
                                    }
                                }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Очистить лог", tint = Color.Blue)
                                }

                                IconButton(onClick = {
                                    logToView = log
                                }) {
                                    Text("Открыть", color = Color.Green)
                                }
                            }
                        }
                    }
                }

                // Окно добавления нового лога
                if (isDialogOpen) {
                    AddLogDialog(
                        onDismiss = { isDialogOpen = false },
                        onSave = { name, path ->
                            coroutineScope.launch {
                                try {
                                    // Проверка, что имя и путь не пусты
                                    if (name.isBlank() || path.isBlank()) {
                                        snackbarHostState.showSnackbar("Имя и путь к логу не могут быть пустыми")
                                    } else if (logsList.any { it.name == name }) {
                                        snackbarHostState.showSnackbar("Лог с таким именем уже существует")
                                    } else {
                                        logsList.add(LogEntry(name, path))
                                        LogDataStore.saveLogs(context, logsList) // Сохранение списка логов
                                        snackbarHostState.showSnackbar("Лог добавлен: $name")

                                        // Закрываем диалог после успешного добавления
                                        isDialogOpen = false
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    snackbarHostState.showSnackbar("Ошибка при добавлении лога")
                                }
                            }
                        },
                        server = selectedServer // Передаем сервер для выбора файла
                    )
                }


                logToView?.let { log ->
                    ViewLogDialog(log = log, onDismiss = { logToView = null }, server = selectedServer)
                }
            }
        }
    )
}

// Модель для хранения информации о логе
@Serializable
data class LogEntry(val name: String, val path: String)

@Composable
fun AddLogDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    server: Server?
) {
    var logName by remember { mutableStateOf("") }
    var logPath by remember { mutableStateOf("") }
    var isFilePickerOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Добавить лог") },
        text = {
            Column {
                OutlinedTextField(
                    value = logName,
                    onValueChange = { logName = it },
                    label = { Text("Имя лога") }
                )
                OutlinedTextField(
                    value = logPath,
                    onValueChange = { logPath = it },
                    label = { Text("Путь к логу") },
                    trailingIcon = {
                        IconButton(onClick = { isFilePickerOpen = true }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Выбрать файл")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (logName.isNotEmpty() && logPath.isNotEmpty()) {
                    onSave(logName, logPath)
                }
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Отмена")
            }
        }
    )

    if (isFilePickerOpen && server != null) {
        FilePickerDialog(server, onFileSelected = { selectedPath ->
            logPath = selectedPath
            isFilePickerOpen = false
        }, onDismiss = { isFilePickerOpen = false })
    }
}

@Composable
fun ViewLogDialog(log: LogEntry, onDismiss: () -> Unit, server: Server?) {
    var logContent by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(log) {
        coroutineScope.launch {
            try {
                if (server != null) {
                    logContent = fetchLogContent(server, log.path)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(log.name) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(logContent)
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Закрыть")
            }
        }
    )
}

suspend fun clearLog(server: Server?, logPath: String) {
    server?.let {
        withContext(Dispatchers.IO) {
            executeSSHCommand(it, "sudo truncate -s 0 $logPath")
        }
    }
}

suspend fun fetchLogContent(server: Server, logPath: String): String {
    return withContext(Dispatchers.IO) {
        executeSSHCommand(server, "cat $logPath")
    }
}

@Composable
fun FilePickerDialog(
    server: Server,
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPath by remember { mutableStateOf("/") }
    var files by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentPath) {
        coroutineScope.launch {
            try {
                files = getFileList(server, currentPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Выберите файл") },
        text = {
            Column {
                Text("Текущая папка: $currentPath")
                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(files) { (fileName, isDirectory) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isDirectory) {
                                        currentPath = if (currentPath.endsWith("/")) "$currentPath$fileName/" else "$currentPath/$fileName/"
                                    } else {
                                        onFileSelected("$currentPath$fileName")
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Text(fileName, color = if (isDirectory) Color.Blue else Color.Black)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Закрыть")
            }
        }
    )
}

suspend fun getFileList(server: Server, directory: String): List<Pair<String, Boolean>> {
    return withContext(Dispatchers.IO) {
        val result = executeSSHCommand(server, "ls -l $directory")
        result.split("\n").filter { it.isNotEmpty() }.map {
            val parts = it.split(" ")
            val isDirectory = it.startsWith("d")
            val fileName = parts.last()
            fileName to isDirectory
        }
    }
}
