package com.example.easyremcontrol.ui.screens

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jcraft.jsch.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.example.easyremcontrol.ui.models.Server
import java.util.Vector
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SFTPClientScreen(
    selectedServer: Server?,  // nullable тип
    context: Context,
    onBackPress: () -> Unit   // Добавляем обработку возврата назад
) {
    var currentPath by remember { mutableStateOf("/") }  // Текущая директория на сервере
    var fileList by remember { mutableStateOf(listOf<String>()) }
    var selectedFiles by remember { mutableStateOf<Set<String>>(emptySet()) }  // Множество выбранных файлов
    val coroutineScope = rememberCoroutineScope()

    // Загружаем список файлов с сервера
    LaunchedEffect(currentPath, selectedServer) {
        selectedServer?.let { server ->  // Проверяем, что selectedServer не null
            val files = connectAndListFiles(server, currentPath)
            fileList = files
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SFTP Client") },
                navigationIcon = {
                    IconButton(onClick = { onBackPress() }) {  // Кнопка возврата
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Путь навигации
                Text("Путь: $currentPath")

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопка для возврата на уровень выше
                if (currentPath != "/") {
                    Button(onClick = {
                        // Логика перемещения на уровень выше
                        currentPath = currentPath.substringBeforeLast("/")
                        if (currentPath.isEmpty()) currentPath = "/"
                    }) {
                        Text("На уровень выше")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Отображаем список файлов и папок
                LazyColumn {
                    items(fileList) { fileName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selectedFiles.contains(fileName)) Color.LightGray else Color.Transparent)
                                .clickable {
                                    if (fileName.endsWith("/")) {
                                        // Переход внутрь папки
                                        currentPath = "$currentPath/$fileName".removeSuffix("/")
                                    } else {
                                        // Выбираем или убираем файл из множества выделенных файлов
                                        if (selectedFiles.contains(fileName)) {
                                            selectedFiles = selectedFiles - fileName
                                        } else {
                                            selectedFiles = selectedFiles + fileName
                                        }
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (fileName.endsWith("/")) "📁 $fileName" else "📄 $fileName",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Меню действий с выбранными файлами
                if (selectedFiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = {
                            coroutineScope.launch {
                                selectedServer?.let { server ->
                                    selectedFiles.forEach { file ->
                                        downloadFileFromServer(server, "$currentPath/$file", context)
                                    }
                                    selectedFiles = emptySet() // Очистка выделения после загрузки
                                }
                            }
                        }) {
                            Text("Загрузить")
                        }

                        Button(onClick = {
                            // Логика для копирования файлов
                        }) {
                            Text("Копировать")
                        }

                        Button(onClick = {
                            // Логика для вставки файлов
                        }) {
                            Text("Вставить")
                        }

                        Button(onClick = {
                            // Логика для удаления файлов
                            selectedFiles = emptySet() // Очистка выделения после удаления
                        }) {
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    )
}

// Функция для подключения к серверу и получения списка файлов
suspend fun connectAndListFiles(server: Server, path: String): List<String> {
    return withContext(Dispatchers.IO) {
        try {
            val jsch = JSch()
            val session = jsch.getSession(server.username, server.ip, 22)
            session.setPassword(server.password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()

            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()

            val files = channel.ls(path) as Vector<ChannelSftp.LsEntry>
            channel.disconnect()
            session.disconnect()

            files.map { if (it.attrs.isDir) "${it.filename}/" else it.filename }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

// Функция для загрузки файла с сервера
suspend fun downloadFileFromServer(server: Server, remoteFilePath: String, context: Context) {
    withContext(Dispatchers.IO) {
        try {
            val jsch = JSch()
            val session = jsch.getSession(server.username, server.ip, 22)
            session.setPassword(server.password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()

            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()

            // Путь для сохранения файла на устройство
            val localFilePath = "${context.filesDir}/$remoteFilePath"
            channel.get(remoteFilePath, localFilePath)

            channel.disconnect()
            session.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}