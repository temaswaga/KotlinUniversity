package com.example.myapplication.Task6

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    companion object {
        const val EXTRA_SECONDS = "EXTRA_SECONDS"
        private const val CHANNEL_ID = "task6_timer_channel"
        private const val NOTIFICATION_ID = 202
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val seconds = intent?.getLongExtra(EXTRA_SECONDS, 0L) ?: 0L

        // Запуск ожидания в отдельном потоке (фоновая корутина)
        serviceScope.launch {
            if (seconds > 0) {
                delay(seconds * 1000L)
            }
            // Показ всплывающего уведомления
            showFinishedNotification()
            // Служба сама останавливает себя
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Таймер",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал завершения одноразового таймера"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showFinishedNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Таймер завершён")
            .setContentText("Время вышло!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}