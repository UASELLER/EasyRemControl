package com.example.easyremcontrol.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.InputStream
import com.example.easyremcontrol.ui.models.Server
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.delay

// Функция для подключения к серверу через SSH и выполнения команды htop
suspend fun connectToSSHAndRunHTOP(server: Server, onOutputReceived: (String) -> Unit): Session? {
    return withContext(Dispatchers.IO) {
        try {
            val jsch = JSch()
            val session = jsch.getSession(server.username, server.ip, 22)
            session.setPassword(server.password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()

            // Открываем канал exec для выполнения команды htop
            val channel = session.openChannel("exec") as ChannelExec
            channel.setCommand("htop")  // Выполнение команды htop
            channel.setPty(true)        // Включаем псевдотерминал для корректного отображения htop
            channel.inputStream = null

            val inputStream: InputStream = channel.inputStream
            channel.connect()

            // Чтение данных с сервера в реальном времени
            val buffer = ByteArray(1024)
            var read: Int
            while (channel.isConnected) {
                while (inputStream.available() > 0) {
                    read = inputStream.read(buffer)
                    if (read > 0) {
                        val output = String(buffer, 0, read)
                        withContext(Dispatchers.Main) {
                            onOutputReceived(output)
                        }
                    }
                }
                delay(500) // Задержка для предотвращения загрузки ЦП
            }

            channel.disconnect()
            session.disconnect()

            session
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HTOPScreen(
    selectedServer: Server?,
    onBackPress: () -> Unit,
    context: Context
) {
    var output by remember { mutableStateOf("Запуск HTOP...") }
    val scrollState = rememberScrollState() // Для прокрутки терминала
    val coroutineScope = rememberCoroutineScope()

    // Используем черный фон и белый текст
    val textColor = Color.White
    val backgroundColor = Color.Black

    // Настройки для шрифта "Courier New" с размером 10sp
    val terminalTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,  // Monospace по умолчанию включает Courier New
        fontSize = 10.sp,                   // Размер шрифта 10
        color = textColor                   // Белый цвет текста
    )

    // Подключаемся к серверу и запускаем htop
    LaunchedEffect(selectedServer) {
        if (selectedServer != null) {
            connectToSSHAndRunHTOP(selectedServer) { serverOutput ->
                output += serverOutput
            }
        } else {
            output = "Сервер не выбран!"
        }
    }

    // Интерфейс для терминала
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HTOP Monitoring") },
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
                    .fillMaxWidth()
            ) {
                Text(output, modifier = Modifier.padding(16.dp), style = terminalTextStyle)
            }
        }
    }
}
