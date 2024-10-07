package com.example.easyremcontrol.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.easyremcontrol.ui.screens.MainMenuScreen
import com.example.easyremcontrol.ui.screens.SettingsScreen
import com.example.easyremcontrol.ui.screens.SSHClientScreen
import com.example.easyremcontrol.ui.screens.HTOPScreen
import com.example.easyremcontrol.ui.screens.LogsScreen
import com.example.easyremcontrol.ui.screens.RebootScreen
import com.example.easyremcontrol.ui.screens.SFTPClientScreen
import com.example.easyremcontrol.ui.screens.ServicesScreen
import com.example.easyremcontrol.ui.models.Server

@Composable
fun NavGraph(
    navController: NavHostController,
    selectedServer: Server?, // nullable тип
    onServerSelected: (Server?) -> Unit, // функция для выбора сервера
    selectedTheme: String,  // Текущая выбранная тема
    onThemeChange: (String) -> Unit,  // Функция для смены темы
) {
    val context = LocalContext.current

    NavHost(navController, startDestination = "main_menu") {
        // Основное меню
        composable("main_menu") {
            MainMenuScreen(
                onNavigate = { destination -> navController.navigate(destination) },
                context = context,
                selectedServer = selectedServer, // Передаем текущий выбранный сервер
                onServerSelected = { server ->
                    // Сохраняем сервер, но не перенаправляем на SSH
                    onServerSelected(server)
                }
            )
        }

        // Экран настроек
        composable("settings") {
            SettingsScreen(
                selectedTheme = selectedTheme,  // Передаем текущую тему
                onThemeChange = onThemeChange,  // Функция для изменения темы
                onBackPress = { navController.popBackStack() },
                context = context
            )
        }

        // Экран SSH
        composable("ssh") {
            selectedServer?.let { server ->  // Проверяем, что selectedServer не null
                SSHClientScreen(
                    selectedServer = server, // Передаем уже не nullable объект
                    onBackPress = { navController.popBackStack() },
                    context = context,
                    isDarkTheme = selectedTheme == "Тёмная" // Пример использования выбранной темы
                )
            } ?: run {
                // Выводим сообщение, если selectedServer == null
                Text("Сервер не выбран, вернитесь и выберите сервер.")
            }
        }

        // Экран SFTP
        composable("sftp") {
            selectedServer?.let { server ->
                SFTPClientScreen(
                    selectedServer = server, // Передаем сервер
                    context = context,
                    onBackPress = { navController.popBackStack() }
                )
            } ?: run {
                Text("Сервер не выбран, вернитесь и выберите сервер.")
            }
        }

        // Экран Services
        composable("services") {
            selectedServer?.let { server ->
                ServicesScreen(
                    selectedServer = server,
                    onBackPress = { navController.popBackStack() }
                )
            } ?: run {
                Text("Сервер не выбран, вернитесь и выберите сервер.")
            }
        }

        // Экран HTOP
        composable("htop") {
            selectedServer?.let { server ->
                HTOPScreen(
                    selectedServer = server,
                    onBackPress = { navController.popBackStack() },
                    context = context
                )
            } ?: run {
                Text("Сервер не выбран, вернитесь и выберите сервер.")
            }
        }

        // Экран Logs
        composable("logs") {
            selectedServer?.let { server ->
                LogsScreen(
                    selectedServer = server,
                    onBackPress = { navController.popBackStack() }
                )
            } ?: run {
                Text("Сервер не выбран, вернитесь и выберите сервер.")
            }
        }

        // Экран Reboot
        composable("reboot") {
            selectedServer?.let { server ->
                RebootScreen(
                    selectedServer = server,
                    onBackPress = { navController.popBackStack() }
                )
            } ?: run {
                Text("Сервер не выбран, вернитесь и выберите сервер.")
            }
        }
    }
}
