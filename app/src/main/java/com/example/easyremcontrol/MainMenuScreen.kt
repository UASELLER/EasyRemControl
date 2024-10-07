package com.example.easyremcontrol.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyremcontrol.ui.datastore.ServerDataStore
import com.example.easyremcontrol.ui.models.Server
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onNavigate: (String) -> Unit,
    context: Context,
    selectedServer: Server?,  // Передаем выбранный сервер
    onServerSelected: (Server?) -> Unit // Функция для выбора сервера
) {
    var servers by remember { mutableStateOf(ServerDataStore.getServers(context)) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EasyRemControl") },
                actions = {
                    IconButton(onClick = { onNavigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
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
                horizontalAlignment = Alignment.CenterHorizontally, // Центрируем весь столбец по горизонтали
                verticalArrangement = Arrangement.Top
            ) {
                // Выпадающий список для выбора сервера
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(selectedServer?.name ?: "Выбрать сервер")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        servers.forEach { server ->
                            DropdownMenuItem(
                                text = { Text(server.name) },
                                onClick = {
                                    onServerSelected(server)  // Передаем выбранный сервер
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Добавляем отступ между выбором сервера и кнопками меню
                Spacer(modifier = Modifier.height(100.dp)) // Отступ 32.dp между выбором сервера и кнопками

                // Сетка из двух столбиков для квадратных кнопок
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Отступы между строками
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center // Центрирование по горизонтали
                    ) {
                        MenuButton(
                            text = "HTOP",
                            onClick = { onNavigate("htop") },
                            enabled = selectedServer != null
                        )
                        Spacer(modifier = Modifier.width(16.dp)) // Отступ между кнопками
                        MenuButton(
                            text = "SSH",
                            onClick = { onNavigate("ssh") },
                            enabled = selectedServer != null
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center // Центрирование по горизонтали
                    ) {
                        MenuButton(
                            text = "SFTP",
                            onClick = { onNavigate("sftp") },
                            enabled = selectedServer != null
                        )
                        Spacer(modifier = Modifier.width(16.dp)) // Отступ между кнопками
                        MenuButton(
                            text = "SERVICES",
                            onClick = { onNavigate("services") },
                            enabled = selectedServer != null
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center // Центрирование по горизонтали
                    ) {
                        MenuButton(
                            text = "LOGS",
                            onClick = { onNavigate("logs") },
                            enabled = selectedServer != null
                        )
                        Spacer(modifier = Modifier.width(16.dp)) // Отступ между кнопками
                        MenuButton(
                            text = "REBOOT",
                            onClick = { onNavigate("reboot") },
                            enabled = selectedServer != null
                        )
                    }
                }
            }
        }
    )
}

// Функция для создания квадратных кнопок с обводкой текста и тенью
@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(150.dp) // Квадратная форма
            .background(
                if (enabled) Color.Green else Color.Gray,
                shape = RoundedCornerShape(12.dp) // Скругленные углы
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold, // Жирный текст
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
