package com.example.easyremcontrol.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.easyremcontrol.R

// Определяем цветовые схемы для разных тем
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1DB954),
    onPrimary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.DarkGray,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    background = Color(0xFFF6F6F6),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

// Определение цветов для темы "Матрица"
private val MatrixColorScheme = darkColorScheme(
    primary = Color(0xFF00FF00), // Зеленый цвет для текста
    onPrimary = Color.Black,
    background = Color.Black, // Черный фон для матрицы
    onBackground = Color(0xFF00FF00), // Зеленый текст на черном фоне
    surface = Color.Black,
    onSurface = Color(0xFF00FF00)
)

@Composable
fun EasyRemControlTheme(
    selectedTheme: String, // Передаем выбранную тему
    content: @Composable () -> Unit
) {
    // Выбираем цветовую схему в зависимости от выбранной темы
    val colors = when (selectedTheme) {
        "Темная" -> DarkColorScheme // Явно проверяем строку "Темная"
        "Светлая" -> LightColorScheme
        "Матрица" -> MatrixColorScheme
        else -> LightColorScheme // fallback на светлую тему по умолчанию
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
    ) {
        if (selectedTheme == "Матрица") {
            // Для темы "Матрица" добавляем фоновое изображение
            Box(modifier = Modifier.fillMaxSize()) {
                // Фоновое изображение
                Image(
                    painter = painterResource(id = R.drawable.matrix_bg),
                    contentDescription = "Matrix Background",
                    contentScale = ContentScale.Crop, // Растягиваем фон на весь экран
                    modifier = Modifier.fillMaxSize() // Устанавливаем изображение на весь экран
                )
                // Контент поверх фона
                content()
            }
        } else {
            // Если не тема "Матрица", просто рендерим контент
            content()
        }
    }
}
