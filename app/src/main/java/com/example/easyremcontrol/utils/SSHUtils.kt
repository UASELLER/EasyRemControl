package com.example.easyremcontrol.utils

import com.example.easyremcontrol.ui.models.Server
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.ChannelExec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

// Функция для выполнения SSH-команд
suspend fun executeSSHCommand(server: Server, command: String): String {
    var session: Session? = null
    var channel: ChannelExec? = null
    return withContext(Dispatchers.IO) {
        var result = ""
        try {
            val jsch = JSch()
            session = jsch.getSession(server.username, server.ip, 22)
            session!!.setPassword(server.password)
            session!!.setConfig("StrictHostKeyChecking", "no")
            session!!.connect(30000)  // Тайм-аут 30 секунд

            // Открываем канал для выполнения команды
            channel = session!!.openChannel("exec") as ChannelExec
            channel!!.setCommand(command)

            // Получаем результат выполнения команды
            val inputStream = BufferedReader(InputStreamReader(channel!!.inputStream))
            channel!!.connect()

            val output = inputStream.use { it.readText() }
            result = output

        } catch (e: Exception) {
            result = "Ошибка: ${e.message}"
        } finally {
            // Безопасно закрываем канал и сессию
            try {
                channel?.disconnect()
            } catch (e: Exception) { }
            try {
                session?.disconnect()
            } catch (e: Exception) { }
        }
        result
    }
}
