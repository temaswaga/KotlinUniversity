package com.example.myapplication.Module4.Task9

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ArrayCreatingInputMerger
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

object WeatherNotificationHelper {
    const val CHANNEL_ID = "weather_channel"
    const val NOTIFICATION_ID = 301

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Прогноз погоды",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, text: String, isOngoing: Boolean = true) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Прогноз погоды")
            .setContentText(text)
            .setOngoing(isOngoing)
            .setOnlyAlertOnce(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }
}

class CityWeatherWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val cityName = inputData.getString("city_name") ?: return Result.failure()
        val temp = inputData.getInt("temp", 0)
        val condition = inputData.getString("condition") ?: "ясно"
        val delayMs = inputData.getLong("delay_ms", 2000L)

        delay(delayMs)

        WeatherNotificationHelper.showNotification(
            applicationContext,
            "Получены данные: $cityName"
        )

        val output = workDataOf(
            "city_name" to cityName,
            "temp" to temp,
            "condition" to condition
        )
        return Result.success(output)
    }
}

class WeatherReportWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        WeatherNotificationHelper.showNotification(
            applicationContext,
            "Все данные получены, формируем отчёт…"
        )

        delay(1500L)

        val temps = inputData.getIntArray("temp") ?: intArrayOf()
        val avgTemp = if (temps.isNotEmpty()) temps.average().toInt() else 0

        WeatherNotificationHelper.showNotification(
            applicationContext,
            "Отчёт готов! Средняя температура ${if (avgTemp > 0) "+$avgTemp" else "$avgTemp"}°C",
            isOngoing = false
        )

        val output = workDataOf("avg_temp" to avgTemp)
        return Result.success(output)
    }
}