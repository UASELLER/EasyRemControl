package com.example.easyremcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.easyremcontrol.navigation.NavGraph
import com.example.easyremcontrol.ui.models.Server
import com.example.easyremcontrol.ui.theme.EasyRemControlTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Управление темой (выбор темы)
            var selectedTheme by remember { mutableStateOf("Светлая") }  // Задаем начальную тему

            // EasyRemControlTheme теперь отвечает за тему всего приложения
            EasyRemControlTheme(selectedTheme = selectedTheme) {
                // Создаем NavController для навигации
                val navController = rememberNavController()

                // Переменная состояния для выбранного сервера
                var selectedServer by remember { mutableStateOf<Server?>(null) }

                // Вызов NavGraph с передачей функции onServerSelected для выбора сервера
                NavGraph(
                    navController = navController,
                    selectedServer = selectedServer, // Передаем текущее значение selectedServer
                    onServerSelected = { server -> selectedServer = server },
                    selectedTheme = selectedTheme,  // Передаем текущую тему
                    onThemeChange = { newTheme ->
                        selectedTheme = newTheme // Обновляем тему
                    }
                )
            }
        }
    }
}
