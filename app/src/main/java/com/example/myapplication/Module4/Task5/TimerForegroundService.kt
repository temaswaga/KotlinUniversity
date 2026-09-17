package com.example.myapplication.Module4.Task5

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null
    private var seconds = 0

    companion object {
        private const val CHANNEL_ID = "counter_foreground_channel"
        private const val NOTIFICATION_ID = 101

        private val _secondsState = MutableStateFlow(0)
        val secondsState = _secondsState.asStateFlow()

        private val _isRunningState = MutableStateFlow(false)
        val isRunningState = _isRunningState.asStateFlow()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (timerJob == null) {
            _isRunningState.value = true

            // Запуск службы переднего плана с явной передачей типа для Android 14+
            val notification = buildNotification(seconds)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            timerJob = serviceScope.launch {
                while (isActive) {
                    delay(1000L)
                    seconds++
                    _secondsState.value = seconds

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(seconds))
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Счётчик времени",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомление активного счётчика времени"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(sec: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Таймер работает")
            .setContentText("Прошло $sec секунд")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        _isRunningState.value = false
        _secondsState.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}