package com.example.easyremcontrol.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.easyremcontrol.ui.models.Server
import com.example.easyremcontrol.utils.executeSSHCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.easyremcontrol.R
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight


@Composable
fun RebootScreen(
    selectedServer: Server,
    onBackPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Добавляем изображение кнопки
        Image(
            painter = painterResource(id = R.drawable.reboot), // Здесь используется сгенерированный ресурс
            contentDescription = "Reboot",
            modifier = Modifier
                .size(150.dp)
                .clickable {
                    // Действие по нажатию кнопки
                    CoroutineScope(Dispatchers.IO).launch {
                        rebootServer(selectedServer)
                    }
                },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

               Text(text = "Reboot Server", color = Color.Red, fontWeight = FontWeight.Bold)
    }
}

// Функция для выполнения перезагрузки сервера
suspend fun rebootServer(server: Server) {
    withContext(Dispatchers.IO) {
        try {
            executeSSHCommand(server, "sudo reboot") // Команда для перезагрузки сервера
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
