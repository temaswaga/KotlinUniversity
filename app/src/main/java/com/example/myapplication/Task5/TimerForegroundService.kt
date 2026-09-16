package com.example.myapplication.Task5

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class TimerForegroundService : Service() {

    private val CHANNEL_ID = "TimerChannel"
    private val NOTIFICATION_ID = 1

    // Scope для запуска таймера параллельно основному потоку
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onBind(intent: Intent?): IBinder? {
        return null // Мы не привязываемся к сервису, поэтому возвращаем null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    // Вызывается при старте сервиса через startService / startForegroundService
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Обязательный вызов для Foreground Service в течение 5 секунд
        startForeground(NOTIFICATION_ID, buildNotification(0))

        // 2. Запуск таймера
        serviceScope.launch {
            TimerState.seconds.value = 0
            while (isActive) {
                delay(1000) // Ждем 1 секунду
                TimerState.seconds.value += 1

                // Обновляем уведомление новым текстом
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, buildNotification(TimerState.seconds.value))
            }
        }

        // START_NOT_STICKY означает, что сервис не будет перезапущен системой, если его убьют
        return START_NOT_STICKY
    }

    // Вызывается при остановке сервиса
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Отменяем таймер
    }

    private fun buildNotification(seconds: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Стандартная иконка
            .setContentTitle("Таймер работает")
            .setContentText("Прошло $seconds секунд")
            .setOngoing(true) // Делает уведомление постоянным (нельзя смахнуть)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Таймер",
                NotificationManager.IMPORTANCE_LOW // LOW, чтобы не пиликало каждую секунду
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}