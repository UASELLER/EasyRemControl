package com.example.easyremcontrol.ui.screens

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.jcraft.jsch.JSch
import com.jcraft.jsch.ChannelShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import com.example.easyremcontrol.ui.models.Server
import androidx.compose.foundation.background
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp



// Функция для подключения к серверу через SSH и открытия интерактивного терминала
suspend fun connectToSSHServer(server: Server): ChannelShell? {
    return withContext(Dispatchers.IO) {
        try {
            val jsch = JSch()
            val session = jsch.getSession(server.username, server.ip, 22)
            session.setPassword(server.password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()

            // Открываем интерактивный Shell канал
            val channel = session.openChannel("shell") as ChannelShell
            channel.setPty(true)  // Активируем псевдотерминал для shell
            channel.connect() // Убеждаемся, что канал подключён
            channel
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// Функция для обработки ввода/вывода с сервера
suspend fun handleSSHCommunication(
    channel: ChannelShell,
    onOutputReceived: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = channel.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val buffer = CharArray(1024)

            // Чтение данных с сервера
            while (channel.isConnected) {
                if (reader.ready()) {
                    val read = reader.read(buffer)
                    if (read > 0) {
                        val output = String(buffer, 0, read)
                        withContext(Dispatchers.Main) {
                            onOutputReceived(output)
                        }
                    }
                }
                delay(100) // Задержка между проверками на наличие данных
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onOutputReceived("\nОшибка связи: ${e.localizedMessage ?: "Неизвестная ошибка"}\n")
            }
            e.printStackTrace()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SSHClientScreen(
    selectedServer: Server?,
    onBackPress: () -> Unit,
    context: Context,
    isDarkTheme: Boolean
) {
    var output by remember { mutableStateOf("Подключение...") }

    var command by remember { mutableStateOf("") }
    var channelShell: ChannelShell? by remember { mutableStateOf(null) }
    val scrollState = rememberScrollState() // Для прокрутки терминала
    val coroutineScope = rememberCoroutineScope()

    // Используем черный фон и белый текст независимо от темы
    val textColor = Color.White
    val backgroundColor = Color.Black

    // Настройки для шрифта "Courier New" с размером 14sp
    val terminalTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,  // Monospace по умолчанию включает Courier New
        fontSize = 14.sp,                   // Размер шрифта 10
        color = textColor                   // Белый цвет текста
    )

    // Подключаемся к серверу при изменении selectedServer
    LaunchedEffect(selectedServer) {
        if (selectedServer != null) {
            output = "Подключаемся к серверу ${selectedServer.name}..."
            channelShell = connectToSSHServer(selectedServer)
            if (channelShell != null) {
                output += "\nПодключение установлено!\n"
                handleSSHCommunication(channelShell!!) { serverOutput ->
                    output += serverOutput
                }
            } else {
                output = "Не удалось подключиться к серверу."
            }
        }
    }

    // Прокрутка к последнему элементу при новом выводе
    LaunchedEffect(output) {
        coroutineScope.launch {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Интерфейс для терминала
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SSH Terminal") },
                navigationIcon = {
                    IconButton(onClick = { onBackPress() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Поле для вывода текста
            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .background(backgroundColor)
                    .fillMaxWidth()  // Убедимся, что вывод заполняет ширину экрана
            ) {
                Text(output, modifier = Modifier.padding(16.dp), color = textColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Поле для ввода команды
            BasicTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray)
                    .background(backgroundColor) // Фон поля ввода черный
                    .padding(16.dp),
                textStyle = terminalTextStyle
            )

            // Кнопка для отправки команды
            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        if (channelShell != null && command.isNotEmpty()) {
                            try {
                                // Отправляем команду
                                val outputStreamWriter = OutputStreamWriter(channelShell!!.outputStream)
                                outputStreamWriter.write(command + "\n")
                                outputStreamWriter.flush()
                                withContext(Dispatchers.Main) {
                                    command = "" // Очищаем поле команды на главном потоке
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    output += "\nОшибка отправки команды: ${e.localizedMessage ?: "Неизвестная ошибка"}\n"
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                output += "\nНе удалось отправить команду. Канал не подключен.\n"
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Send Command")
            }
        }
    }
}
